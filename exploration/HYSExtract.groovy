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


// This class brings together all the generic functions to do with extracting records from sheffield help yourself
class HYSExtract {

  def reader = new HYSHTMLRecordReader()

  def processTopLevel(all_records) {

    processSearchForAll(all_records,"full")
    // This one extracts categories for records. If we already have the record, it won't be re-read.
    processTopLevel(all_records,"full")
  }

  def processSearchForAll(all_records,mode) {
    def simple_search = new HTTPBuilder( 'http://www.sheffieldhelpyourself.org.uk/simple_search_description.asp' )
    try {
      def response = simple_search.post(
      body: [
        searchname: "%",
        // contentType: groovyx.net.http.ContentType.TEXT,
        contentType: "text/html; charset=UTF8",
        requestContentType: URLENC
      ]) {  resp, parsed_page ->
        def links = parsed_page.depthFirst().findAll{ it.name() == 'A' && it.@href.toString().startsWith("full_search_new") }
        println "links: ${links.size()}"
        links.each { row ->
          def uri = "${row.@href}"
          def internal_id = uri.substring(uri.firstIndexOf('=')+1, uri.firstIndexOf('&'))
          println "Processing ${internal_id}"
          println "${row.@href} internal id is ${internal_id}"
          def current_record = all_records[internal_id]
          if ( current_record != null ) {
            // Already in memory
          }
          else {
            current_record = reader.readRecord(internal_id)
            all_records[internal_id]  = current_record;
          }
        }
      }
    }
    catch ( Exception e ) {
        println "Problem ${e}"
        e.printStackTrace()
    }
    finally {
    }
  }

  def processTopLevel(all_records, mode) {
    def base_url = "http://www.sheffieldhelpyourself.org.uk/"
  
    println "Loading page"
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
    println "Done.. Parse"
  
  
    // 1. Find the select element
    def select_element = response_page.depthFirst().findAll { it.name() == 'SELECT' }
  
    def options = select_element[0].depthFirst().findAll { it.name() == 'OPTION' }

    //If in test mode, only do the first one
    if ( mode == 'TEST' ) {
      options = [options[0]];
    }
  
    options.each {
      // println "option ${it.text()}"
      // We now need to post a form to http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp 
      // with the field "category" set to the text above.
     
      // Keyword searches can be done with http://www.sheffieldhelpyourself.org.uk/keyword_search.asp?keyword=RECYCLING
      def simple_search = new HTTPBuilder( 'http://www.sheffieldhelpyourself.org.uk/simple_search_cat.asp' )
      // println "Parsers: ${simple_search.getParser().buildDefaultParserMap()}"
  
      try {
        def response = simple_search.post(
        body: [
          category: "${it.text()}",
          // contentType: groovyx.net.http.ContentType.TEXT,
          contentType: "text/html; charset=UTF8",
          requestContentType: URLENC
        ]) {  resp, parsed_page ->
  
          def links = parsed_page.depthFirst().findAll{ it.name() == 'A' && it.@href.toString().startsWith("full_search_new") }
          println "links: ${links.size()}"
          links.each { row ->
            def uri = "${row.@href}"
            def internal_id = uri.substring(uri.lastIndexOf('=')+1)
            println "${row.@href} internal id is ${internal_id}"
            def current_record = all_records[internal_id]
            if ( current_record != null ) {
               current_record["category"].add("${it.text()}".toString())
               println "Record ${internal_id} added category ${it.text()}"
            }
            else {
               current_record = reader.readRecord(internal_id)
               current_record["category"] = ["${it.text()}".toString()]
               all_records[internal_id]  = current_record;
               println "Created Record for ${internal_id} with category ${it.text()}"
            }
           
            println "Now holding ${all_records.size()} records"
          }
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
  }
}
