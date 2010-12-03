@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

// Read test reading www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=16442
// Other goog test records: http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=23786
// result = readRecord(16442)
// result = readRecord(23786)


class HYSHTMLRecordReader {
  
  // Reads the record at http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=x
  // and returns a map of the properties for that resource.
  def readRecord(id) {
    def result = [:]
    def base_url = "http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=${id}"

    addProperty(result,"BaseURL",base_url)
    addProperty(result,"HYSID",id)
  
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
    // def links = myDocument.depthFirst().A['@href'].findAll{ it.endsWith(".xml") }
    // def response_text = base_url.toURL().text
    // def response_xml = new XmlSlurper().parseText(response_text)
  
    addProperty(result,"OrgName", extract2(response_page, 'Organisation Name'))
    addProperty(result,"AltName", extract2(response_page, 'Alternative Name'))
    addProperty(result,"Desc", extract2(response_page, 'Description'))
    addProperty(result,"Website", extract2(response_page, 'Local Website'))
    addProperty(result,"Langs", extract2(response_page, 'Community Languages'))
    addProperty(result,"Pubs", extract2(response_page, 'Publications'))
    addProperty(result,"CharityNo", extract2(response_page, 'Charity Number'))
  
    processContactDetails(response_page, result);
  
    println "\n\n${result}"
  
    result
  }
  
  /**
   *   This is a very rough and ready util to scrape data when presented in a table of name-value pairs...
   *   It's slight sophistocation is that it can dig into arbitrary elements on both sides to identify the field and the value
   */
  def extract2(page, field) {
    // println "Looking for ${field}"
  
    def result = ""
    def target_rows = page.depthFirst().findAll{ it.text() =~  ".*${field}.*" }
    // println "rows: ${target_rows}"
    target_rows.each {
      // Try to find a parent tr so we can navigate down to the content of td[1] - IE the real data
      def elem = it
      while ( ( elem != null ) && ( elem.name() != 'TR' ) ) {
        elem = elem.parent()
        // println "Testing ${elem.name()}"
      }
  
      if ( elem != null ) {
        // println "Processing ${elem.'**'.text()}"
        // println "Found parent.. Hopefully content = ${elem.text()}"
        if ( result.length() > 0 )
          result += " "
        result += "${elem.TD[1].'**'.text()}"
      }
      else {
        // println "didn't find parent"
      }
    }
    result
  }
  
  def processContactDetails(page, result) {
    // The contact details section is slightly more complex. It consists of a header block of generic contact information
    // Followed by a repeating block for each activity or service which includes address, date/time etc
    def contact_info = page.depthFirst().findAll{ it.text() =~  ".*Contact Details.*" }
    if ( ( contact_info != null ) && ( contact_info.size() > 0 ) ) {
      def contact_info_node = contact_info[0];
      def contact_tr = contact_info_node.'..'.'..'.'..'
      def font_elements = contact_tr.depthFirst().findAll { it.name() == 'FONT' }
      // Extract all the strings....
      def contact_strings = []
      font_elements.each {
        // def this_str = it.'**'.text()
  
        if ( ( it.B != null ) && ( it.B.text().length() > 0 ) ) {
          // println "Slipping in a heading ${it.B.text()}"
          contact_strings.add(it.B.text())
        }
        else if ( ( it.A != null ) && ( it.A.text().length() > 0 ) ) {
          contact_strings.add(it.A.text())
        }
  
        def this_str = it.text()
        if ( this_str.length() > 0 ) {
          contact_strings.add(this_str)
          // println "Got str ${this_str}"
        }
      }
      
      def parse_config = [
                           "ContactDetails":["ContactDetails",""],
                           "Address:":["Address",""],
                           "Disabled Access Details:":["DisabledAccess",""],
                           "Contact Name:":["ContactName",""],
                           "Telephone 1:":["Telephone",""],
                           "Telephone 2:":["Telephone",""],
                           "Fax:":["Fax",""],
                           "Email:":["Email",""],
                           "Service/Activity Details:":["ProviderServiceDetails"],
                           " (Please click on the postcode link to view location on Multimap)":["postcode"],
                           "Further Access Details:":["DisabledAccess"]
                         ]
  
      def field = "none"
      contact_strings.each {
        if ( parse_config[it] != null ) {
          // println "config ${it} ${parse_config[it]}"
          field = parse_config[it][0];
        }
        else {
          addProperty(result,field,it)
          // println "${field}=${it}"
        }
      }
  
      def service_details = page.depthFirst().findAll{ it.text() ==  "Service/Activity Details" }
      // println "Found ${service_details}"
  
      field = "none"
  
      def service_details_config = [
                                     "Service/Activity Details":["ProvisionPlace"],
                                     " (Please click on the postcode link to view location on Multimap)":["ProvisionPlacePostcode"],
                                     "Disabled Access Details:":["ProvisionPlaceDisabledAccess"],
                                     "Days and Times:":["ProvisionDaysAndTimes"],
                                     "Telephone 1:":["Telephone"],
                                     "Telephone 2:":["Telephone"]
                                   ]
  
  
      service_details.each {
        def service_strings = []
  
        def sd_table = it.'..'.'..'.'..'.'..'.'..'
        // println "\n\ntable element: ${sd_table.name()}"
        def sd_font_elements = sd_table.depthFirst().findAll { it.name() == 'FONT' }
        // Extract all the strings....
        sd_font_elements.each {
          if ( ( it.B != null ) && ( it.B.text().length() > 0 ) ) {
            // println "Slipping in a heading ${it.B.text()}"
            service_strings.add(it.B.text())
          }
          else if ( ( it.A != null ) && ( it.A.text().length() > 0 ) ) {
            service_strings.add(it.A.text())
          }
  
          def this_str = it.text()
          if ( this_str.length() > 0 ) {
            service_strings.add(this_str)
          }
        }
  
        // println "Processing using config ${service_details_config}"
        def service_props = [:]
        service_strings.each { ss ->
          // println "Testing *${ss}* ${service_details_config.contains(ss)}"
          if ( service_details_config[ss] != null ) {
            // println "next field : ${it} ${service_details_config[it]}"
            field = service_details_config[ss][0];
          }
          else {
            // println "set field ${field}=${ss}"
            addProperty(service_props,field,ss)
          }
        }
  
        addProperty(result,"Provision",service_props)
        // println "\n\nAdding provision ${service_props}"
      }
    }
  }
  
  def addProperty(mapobj, name, value) {
    if ( ( value != null ) && ( "${value}".length() > 0 ) && ( name != "none" ) ) {
      def value_array = mapobj[name]
      def n_value = value;

      if ( value instanceof org.codehaus.groovy.runtime.GStringImpl )
        n_value = value.toString();

      if ( value_array != null ) {
        value_array.add(n_value)
      }
      else {
        mapobj[name] = [n_value]
      }
    }
  }
}
