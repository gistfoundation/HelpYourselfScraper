@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
    @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.0'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import groovy.util.slurpersupport.GPathResult
import org.apache.http.*
import org.apache.http.protocol.*



try{
  def is = new ObjectInputStream(new FileInputStream('serializedMapsOfHYSData.obj'))
  def m = is.readObject()
  is.close()

  def url = "http://localhost:8080"

  def api = new RESTClient(url)
  def rest_upload_pass = ""
  System.in.withReader {
    print 'upload pass:'
    rest_upload_pass = it.readLine()
  }


  // Add preemtive auth
  api.client.addRequestInterceptor( new HttpRequestInterceptor() {
    void process(HttpRequest httpRequest, HttpContext httpContext) {
      String auth = "admin:${rest_upload_pass}"
      String enc_auth = auth.bytes.encodeBase64().toString()
        httpRequest.addHeader('Authorization', 'Basic ' + enc_auth);
      }
    })


  m.each { key, value ->

    api.request(POST) { request ->
      def record = prettyPrint(toJson(value))
      requestContentType = 'multipart/form-data'
      uri.path='/admin/api/helpyourself/upload'
      def multipart_entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      // multipart_entity.addPart("owner", new StringBody( 'ofsted', 'text/plain', Charset.forName('UTF-8')))
      def uploaded_file_body_part = new org.apache.http.entity.mime.content.ByteArrayBody(record.getBytes('UTF8'), 'text/xml', "${value.id}.json");
      multipart_entity.addPart("tf", uploaded_file_body_part);

      request.entity = multipart_entity

      response.success = { resp, data ->
        println("OK - Record uploaded");
      }

      response.failure = { resp ->
        println("Error - ${resp.status}");
        System.out << resp
        println("Done\n\n");
      }
    }
  }

}finally{}
