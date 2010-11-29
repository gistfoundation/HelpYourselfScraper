@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

// Read test reading www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=16442
// Other goog test records: http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=23786
// result = readRecord(16442)
result = readRecord(23786)


def readRecord(id) {
  result = [:]
  def base_url = "http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=${id}"

  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
  // def links = myDocument.depthFirst().A['@href'].findAll{ it.endsWith(".xml") }
  // def response_text = base_url.toURL().text
  // def response_xml = new XmlSlurper().parseText(response_text)

  def org_name = extract2(response_page, 'Organisation Name')
  def alt_name = extract2(response_page, 'Alternative Name')
  def description = extract2(response_page, 'Description')
  def url = extract2(response_page, 'Local Website')
  def languages = extract2(response_page, 'Community Languages')
  def publications = extract2(response_page, 'Publications')
  def charity_no = extract2(response_page, 'Charity Number')

  processContactDetails(response_page);

  println "Name: \"${org_name}\""
  println "Description: \"${description}\""
  println "URL: \"${url}\""
  println "Languages: \"${languages}\""
  println "Publications: \"${publications}\""
  println "Charity Number: \"${charity_no}\""

  result
}

/**
 *   This is a very rough and ready util to scrape data when presented in a table of name-value pairs...
 *   It's slight sophistocation is that it can dig into arbitrary elements on both sides to identify the field and the value
 */
def extract2(page, field) {
  println "Looking for ${field}"

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
      println "didn't find parent"
    }
  }
  result
}

def processContactDetails(page) {
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
        println "Slipping in a heading ${it.B.text()}"
        contact_strings.add(it.B.text())
      }

      def this_str = it.text()
      if ( this_str.length() > 0 ) {
        contact_strings.add(this_str)
        println "Got str ${this_str}"
      }
    }
    
    def parse_config = [
                         "ContactDetails":["ContactDetails",""],
                         "Address:":["Address",""],
                         "Disabled Access Details:":["DisabledAccess",""],
                         "Contact Name:":["ContactName",""],
                         "Telephone 1:":["Telephone",""],
                         "Fax:":["Fax",""],
                         "Email:":["Email",""],
                         "Service/Activity Details:":["","newsvc"]
                       ]

    contact_strings.each {
      if ( parse_config[it] != null ) {
        println "config ${it} ${parse_config[it]}"
      }
      else {
        println "${it}"
      }
    }

    // Now... there is a table for each Service/Activity Details record...
    def service_details = page.depthFirst().findAll{ it.text() ==  "Service/Activity Details" }
    println "Found ${service_details}"

    service_details.each {
      def sd_table = it.'..'.'..'.'..'.'..'.'..'
      println "\n\ntable element: ${sd_table.name()}"
      def sd_font_elements = sd_table.depthFirst().findAll { it.name() == 'FONT' }
      // Extract all the strings....
      sd_font_elements.each {
        if ( ( it.B != null ) && ( it.B.text().length() > 0 ) ) {
          println "Slipping in a heading ${it.B.text()}"
          // contact_strings.add(it.B.text())
        }

        def this_str = it.text()
        if ( this_str.length() > 0 ) {
          // contact_strings.add(this_str)
          println "Got str ${this_str}"
        }
      }
    }

    // Now process the data we've extracted...
    // It goes like this
    //  - "Contact Details"
    //  - "Address"
    //  - Everyting now until we encounter " (Please click on the ..." Is the first address.. Sort out the 5line stuff later :(
    //  - "Disabled Access Details:" [May be missing]
    //  - Now we start with repeating sections...
    //      - "Contact Name"
    //      - The names of any contacts
    //      - "Telephone 1"
    //      - Telephone 
    //      - "Fax"+
    //      - "Email"+
    //      - "Service/Activity Details"
    // Repeat the group
  }
}
