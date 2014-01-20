@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset


def the_base_url = "http://www.sheffieldhelpyourself.org.uk/"

// This is a bit grotty, but the DB isn't big enough to worry about, so cram it into memory whilst we reconstruct
// the full set of subject headings we find against each record (They aren'd displayed so we need to x-ref the urls where we find pages)
def rec_map = [:]
def keyword_map = [:]
processTopLevel(the_base_url, rec_map, keyword_map);

rec_map.each { key, value ->
  println("id: ${key}, keywords:${value.keywords}");
}

println("${rec_map.size()} Resources");
println("${keyword_map.size()} Keywords");

try{
  def out= new ObjectOutputStream(new FileOutputStream('serializedMapsOfHYSData.obj'))
  out.writeObject(rec_map)
  out.close()
}finally{}



def processTopLevel(base_url, rec_map, keyword_map) {

  
  println "Loading page"
  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
  println "Done.. Parse"
  
  // <a href="sport.asp"><img src="images/SportButton.jpg" alt="Sport image" border="0" />
  // def select_element = response_page.depthFirst().A.findAll()
  def select_element = response_page.depthFirst().A.findAll { it.'@class'=='title5' }

  println("Got elements");

  select_element.each { se ->
    processTopLevelHeading(se.'@href', base_url, rec_map, keyword_map);
  }

  println("Done");
}

def processTopLevelHeading(heading, base_url, rec_map, keyword_map) {
  try {
    println("Processing ${heading}");
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url+heading)

    // Extract all links of the form http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=ABSEILING

    def keyword_links =  response_page.depthFirst().A.findAll { it.'@href'?.contains('keyword_search.asp?keyword=')}

    keyword_links.each { kwl ->
      def keyword_url = kwl.'@href'
      def eq_pos = keyword_url.lastIndexOf('=')+1;
      def keyword=keyword_url.substring(eq_pos, keyword_url.length())
      println('Keyword: '+keyword);
      if ( keyword_map[keyword]==null ) {
        keyword_map[keyword] = [:]
      }
      processKeywordSearch(heading,keyword, rec_map, keyword_map);
    }

    //http://www.sheffieldhelpyourself.org.uk/welfare_search.asp?code1=HY/GAY
    def welfare_search_links = response_page.depthFirst().A.findAll { it.'@href'?.contains('welfare_search.asp?code1=')}

    welfare_search_links.each { kwl ->
      def keyword_url = kwl.'@href'
      def eq_pos = keyword_url.lastIndexOf('=')+1;
      def keyword=keyword_url.substring(eq_pos, keyword_url.length())
      println('WelfareSearchCode: '+kwl.'@href')
    }

  }
  catch ( Exception e ) {
    e.printStackTrace();
  }
}

def processKeywordSearch(heading, keyword, rec_map, keyword_map) {
  println("Processing search using http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=${keyword}");
  try {
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse("http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=${keyword}")

    // http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=23453
    def records = response_page.depthFirst().A.findAll { it.'@href'?.contains('full_search_new.asp?group=') }

    records.each { rec ->
      record_url = rec.'@href'
      record_eq_pos = record_url.lastIndexOf('=')+1
      record_id = record_url.substring(record_eq_pos,record_url.length());
      println("Record : ${record_id} seen under heading ${heading} in keyword ${keyword}");

      if ( rec_map[record_id] == null ) {
        rec_map[record_id] = [:]
        rec_map[record_id].id = record_id
        rec_map[record_id].keywords = [keyword]
        processRecord(rec_map[record_id], record_id)
      }
      else {
        rec_map[record_id].keywords.add(keyword);
      }
    }
  }
  catch ( Exception e ) {
    e.printStackTrace();
  }
}

def processRecord(rec, record_id) {

  println("Process record ${record_id}");

  try {
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse("http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=${record_id}")
 
    def details_div = response_page.BODY.DIV.findAll { it.'@align'='left' }
    if ( details_div.size() == 1 ) {
      def details_table = details_div[0].TABLE.TBODY
      def title_row = details_table.TR[0]

      // Annoyingly, there is an optional second title row which has former titles in.
      def description_row = details_table.TR[1]
      def unknown_row_2 = details_table.TR[2]
      def url_row = details_table.TR[3]

      rec.title = title_row.TD.FONT.B.text()
      rec.description = description_row.TD.FONT.text()
      rec.url = url_row.text()

      details_table[0].depthFirst().IMG.findAll{ it.'@src'=='images/envelope.gif'}.each { adi ->
        println("Got address ${adi.parent().text()}");
        println("Process address icon...${adi.parent()}");
        def current_property = null
        def parent_td = adi.parent()
        parent_td.each { ae ->
          println("Consider ${ae}");
          if ( ae.name() == 'IMG' ) {
            switch ( ae.'@src' ) {
              case 'images/envelope.gif':
                current_property='address'
                break;
              case 'images/wheelchairaccesssmall.gif':
                current_property='access'
                break;
              case 'images/telephone.gif':
                current_property='telephone'
                break;
              case 'images/email.gif':
                current_property='email'
                break;
            }
          }
          else if ( ae.name() == 'FONT' ) {
            if ( ae.B.size() > 0 ) {
              println("Got a B element -${ae.B.text()}- it names a property");
              switch ( ae.B.text() ) {
                case 'Address:':
                  current_property='address'
                  break;
                case 'Contact Name:':
                  current_property='contact'
                  break;
                case 'Days and Times:':
                  current_property='daysAndTimes'
                  break;
                case 'Disabled Access Details:':
                  current_property='access'
                  break;
                case 'Email:':
                  current_property='email'
                  break;
                case 'Fax:':
                  current_property='fax'
                  break;
                case 'Further Access Details:':
                  current_property='access'
                  break;
                case 'Minicom:':
                  current_property='minicom'
                  break;
                case 'Mobile:':
                  current_property='mobile'
                  break;
                case 'Telephone Details:':
                case 'Telephone 2 Details:':
                case 'Telephone 3 Details:':
                  current_property='telephoneDetails'
                  break;
                case 'Telephone:':
                case 'Telephone 2:':
                case 'Telephone 3:':
                  current_property='telephone'
                  break;
              }

            }
            else {
              if ( current_property != null ) {
                if ( rec[current_property] == null ) {
                  rec[current_property] = []
                }
                rec[current_property].add(ae.text());
              }
            }
          }
        }
      }
      println("Processed ${rec}");
    }
    else {
      println("Found ${details_div.size()} matching elements");
    }
  }
  catch ( Exception e ) {
    println("Problem processing record with ID ${record_id}")
    e.printStackTrace()
  }
}

