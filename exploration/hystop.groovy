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
    // println "option ${it.text()}"
    // We now need to post a form to http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp 
    // with the field "category" set to the text above.
   
    // Keyword searches can be done with http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=RECYCLING
    def simple_search = new HTTPBuilder( 'http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp' )
    println "Parsers: ${simple_search.getParser().buildDefaultParserMap()}"


    try {
      def response = simple_search.post(
      body: [
        category: "${it.text()}",
        // contentType: groovyx.net.http.ContentType.TEXT,
        contentType: "text/html; charset=UTF8",
        requestContentType: URLENC
      ]) {  resp, parsed_page ->

        // HTTP Response will be automatically parsed out... and put into parsed_page
        // Closure called on ok request
        // def parsed_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(reader.text)
        
        // println "body ${parsed_page.BODY}"
        // println "form ${parsed_page.BODY.FORM}"
        // println "table[1] ${parsed_page.BODY.FORM.TABLE}"

        // Now we need to parse out each row in the table..
        def tables = parsed_page.BODY.FORM.children().findAll { x -> x.name()=='TABLE' }

        println "Size of tables array: ${tables.size()}"

        // println "Table[0]: ${tables[0].text()}"

        def table_rows = tables[0].depthFirst().findAll { x -> x.name()=='TR' }

        println "Table rows: ${table_rows.size()}"

        def first = true
        table_rows.each { row ->
          if ( first ) {
            // skip header
            first = false
          }
          else {
            println "Row ${row}"
          }
        }

        // println "Root table: ${table_rows.text()}"
      }

      Thread.sleep(1000)
    }
    catch ( Exception e ) {
      println "Problem ${e}"
      e.printStackTrace()
    }
    finally {
    }
  }

  result
}
