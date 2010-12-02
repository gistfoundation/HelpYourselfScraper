@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])


def all_records = [:]
def extract = new HYSExtract()

extract.processTopLevel(all_records);

all_records.each {
  createXML(it);
}

def createXML(record) {

}
