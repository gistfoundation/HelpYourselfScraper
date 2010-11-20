

# Read test reading www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=16442
result = readRecord(16442)


def readRecord(id) {
  result = [:]
  def base_url = "http://www.sheffieldhelpyourself.org.uk/full_search_new.asp?group=${id}"

  def response_text = base_url.toURL().text
  def response_xml = new XmlSlurper().parseText(response_text)


  result
}
