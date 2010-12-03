@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])


def all_records = [:]
def extract = new HYSExtract()

extract.processTopLevel(all_records,'TEST');

output_path = "./files"
java.io.File store_dir = new java.io.File(output_path)
store_dir.mkdirs()

all_records.each {
  println "Processing ${it}"
  createXML(it.value,output_path);
}

def createXML(record, output_path) {
  println("Processing ${record.HYSID[0]}")
  def output_file = new File("${output_path}/${record.HYSID[0]}.jser")
  output_file << record
}
