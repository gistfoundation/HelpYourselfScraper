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
processTopLevel(the_base_url);


def processTopLevel(base_url) {

  
  println "Loading page"
  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
  println "Done.. Parse"
  
  // <a href="sport.asp"><img src="images/SportButton.jpg" alt="Sport image" border="0" />
  // def select_element = response_page.depthFirst().A.findAll()
  def select_element = response_page.depthFirst().A.findAll { it.'@class'=='title5' }

  println("Got elements");

  select_element.each { se ->
    processTopLevelHeading(se.'@href', base_url);
  }

  println("Done");
}

def processTopLevelHeading(heading, base_url) {
  try {
    println("Processing ${heading}");
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url+heading)

    // Extract all links of the form http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=ABSEILING

    def keyword_links =  response_page.depthFirst().A.findAll { it.'@href'?.contains('keyword_search.asp?keyword=')}

    keyword_links.each { kwl ->
      def keyword_url = kwl.'@href'
      def eq_pos = keyword_url.lastIndexOf('=');
      def keyword=keyword_url.substring(eq_pos, keyword_url.length())
      println('Keyword: '+kwl.'@href')
    }

    //http://www.sheffieldhelpyourself.org.uk/welfare_search.asp?code1=HY/GAY
    def welfare_search_links = response_page.depthFirst().A.findAll { it.'@href'?.contains('welfare_search.asp?code1=')}

    welfare_search_links.each { kwl ->
      def keyword_url = kwl.'@href'
      def eq_pos = keyword_url.lastIndexOf('=');
      def keyword=keyword_url.substring(eq_pos, keyword_url.length())
      println('WelfareSearchCode: '+kwl.'@href')
    }

  }
  catch ( Exception e ) {
    e.printStackTrace();
  }
}

