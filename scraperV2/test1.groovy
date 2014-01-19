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
        rec_map[record_id].keywords = []
      }

      rec_map[record_id].keywords.add(keyword);
    }
  }
  catch ( Exception e ) {
    e.printStackTrace();
  }
}

