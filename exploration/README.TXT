curl --verbose --request PUT --header Content-Type: text/xml --data @./files/18216.xml http://localhost:8080/fim/item

for file in `ls ./files/*.xml`
do
  curl --verbose --request PUT --header Content-Type: text/xml --data @$file http://localhost:8080/fim/item
done
