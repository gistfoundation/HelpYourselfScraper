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

  // def reader = new HYSHTMLRecordReader()

  def processTopLevel(all_records, mode) {
    def base_url = "http://www.sheffieldhelpyourself.org.uk/"
  
    println "Loading page"
    def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
    println "Done.. Parse"
  
  
    // 1. Find the select element
    def select_element = response_page.depthFirst().findAll { it.name() == 'a'}

    
  }
}
