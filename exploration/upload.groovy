@Grapes([
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1')])

import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset

def app = new HTTPBuilder( 'http://localhost:8080/fim/item' )

new File('./files').eachFileMatch( ~/.*\.xml/ ) { 
   println "Uploading ${it}"
   try {

    // uri.path = 'data/data_file.zip'
 
     app.request(PUT) {request ->
        println "Sending..."
        // send("application/xml", it)
        send("text/xml", it)

        response.'201' = { resp ->
            println "201: ${resp.status}"
        }
 
        response.success = { resp, object ->
            println "ok ${resp.status}"
        }
 
        response.failure = { resp ->
            println "failure: ${resp.statusLine}"
        }
     }

     println "Uploaded"
   }
   catch ( Exception e ) {
     println "Problem ${e}"
     e.printStackTrace()
   }
   finally {
     println "Complete"
   }
} 

