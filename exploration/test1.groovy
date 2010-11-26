@Grapes([
    @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
    @Grab(group='xerces', module='xercesImpl', version='2.9.1') ])

// Read test reading www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=16442
result = readRecord(16442)


def readRecord(id) {
  result = [:]
  def base_url = "http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=${id}"

  def response_page = new XmlParser( new org.cyberneko.html.parsers.SAXParser() ).parse(base_url)
  // def links = myDocument.depthFirst().A['@href'].findAll{ it.endsWith(".xml") }
  // def response_text = base_url.toURL().text
  // def response_xml = new XmlSlurper().parseText(response_text)

  def org_name = extract(response_page,'Organisation Name')
  println "org name ${org_name}"

  extract2(response_page, 'Organisation Name')

  result
}

def extract(page,field) {
  def target_rows = page.html.body.'**'.td.find{it.text() =~ ".*${field}.*" }
  println "rows: ${target_rows}"
  "hello"
}

def extract2(page, field) {
  // def target_rows = page.depthFirst().findAll{ it.td.text() =~  ".*${field}.*" }
  // def target_rows = page.depthFirst().findAll{ true }
  def target_rows = page.depthFirst().findAll{ it.text() =~  ".*${field}.*" }
  println "rows: ${target_rows}"
}
