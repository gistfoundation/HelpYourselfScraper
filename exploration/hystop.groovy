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

def a = processTopLevel()

def processTopLevel() {
  result = [:]
  def base_url = "http://www.sheffieldhelpyourself.org.uk/"

  println "Loading page"
  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
  println "Done.. Parse"

  // 1. Find the select element
  def select_element = response_page.depthFirst().findAll { it.name() == 'SELECT' }

  def options = select_element[0].depthFirst().findAll { it.name() == 'OPTION' }

  options.each {
    println "option ${it.text()}"
    // We now need to post a form to http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp 
    // with the field "category" set to the text above.
   
    // Keyword searches can be done with http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=RECYCLING
    def simple_search = new HTTPBuilder( 'http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp' )

    try {
      def response = simple_search.post(
      body: [
        category: "${it.text()}",
        contentType: "text/html; charset=utf-8"
      ])

      println "Response: ${response.data.text}"

      Thread.sleep(1000)
    }
    catch ( Exception e ) {
      println "Problem ${e}"
    }
    finally {
    }
  }

  result
}
