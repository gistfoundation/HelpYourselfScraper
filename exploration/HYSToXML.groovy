@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1'),
    @Grab(group='com.thoughtworks.xstream', module='xstream', version='1.3.1') ])

import com.thoughtworks.xstream.*

def all_records = [:]
def extract = new HYSExtract()

// extract.processTopLevel(all_records,'TEST');
extract.processTopLevel(all_records,'FULL');

output_path = "./files"
java.io.File store_dir = new java.io.File(output_path)
store_dir.mkdirs()
def xstream = new XStream()

all_records.each {
  println "Processing ${it}"
  // createXML(it.value,output_path,xstream);
  createXML2(it.value,output_path)
}

def createXML(record, output_path,xstream) {
  println("Processing ${record.HYSID[0]}")
  new File("${output_path}/${record.HYSID[0]}.xml").withOutputStream { out ->
    xstream.toXML(record,out)
  }
}

def createXML2(record, output_path) {
  println("Processing ${record.HYSID[0]}")
  def outfile = new File("${output_path}/${record.HYSID[0]}.xml")

  if ( outfile.exists() )
    outfile.delete();

  def writer = outfile.newWriter("UTF-8", true)
  writer.write("<SheffieldHYS id=\"${record.HYSID[0]}\">\n")
  dumpMap(record,writer)
  writer.write("</SheffieldHYS>\n")
  writer.close()
}

def dumpMap(mapobj, writer) {
  mapobj.each {
    writer.write("<${it.key}>")
    if ( it.value instanceof Map ) {
      dumpMap(it.value)
    }
    else if ( it.value instanceof List ) {
      it.value.each { listent ->
        if ( listent instanceof Map )
          dumpMap(listent,writer)
        else
          writer.write("<entry>${listent}</entry>\n")
      }
    }
    else {
      writer.write(it.value.toString())
    }
    writer.write("</${it.key}>\n")
  }

}
