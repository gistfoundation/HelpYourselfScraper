@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

def a = processTopLevel()

def processTopLevel() {
  result = [:]
  def base_url = "http://www.sheffieldhelpyourself.org.uk/"

  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)

  // 1. Find the select element
  def select_element = response_page.depthFirst().findAll { it.name() == 'SELECT' }

  def options = select_element[0].depthFirst().findAll { it.name() == 'OPTION' }

  options.each {
    println "option ${it.text()}"
  }

  result
}
