import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.SQLException;

public class xmlParser {
    //DOM
    public static void writeXML(JSONArray jsonArrayData, String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            File file = new File(filePath);
            if (file.length() == 0) {
                document = factory.newDocumentBuilder().newDocument();
                Element root = document.createElement("root");
                document.appendChild(root);
            } else {
                document = builder.parse(filePath);
            }
            document.getDocumentElement().normalize();

            fillTable(document, jsonArrayData);
            document.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (SAXParseException exception) {
            System.out.println("ERRROR");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private static void fillTable(Document doc, JSONArray jsonArrayData) throws SQLException {
        NodeList table = doc.getElementsByTagName("table");
        Element lang = null;
        int i = table.getLength();
        lang = (Element) table.item(i - 1);

        Element tr_head = doc.createElement("tr");
        String tabSymbols = "\t\t\t\t\t";

        System.out.println("size = " + jsonArrayData.size());

        String firstJSONObj = jsonArrayData.get(0).toString();

        firstJSONObj = firstJSONObj.replaceAll("\"", "");
        firstJSONObj = firstJSONObj.substring(1,firstJSONObj.length());
        firstJSONObj = firstJSONObj.substring(0,firstJSONObj.length()-1);

        String arr_col_names[] = firstJSONObj.split(",");

        for(int it = 0;it < jsonArrayData.size();it++){
            Element th = doc.createElement("th");

            th.appendChild(doc.createTextNode(arr_col_names[it]));

            tr_head.appendChild(th);
            tr_head.appendChild(doc.createTextNode("\n"));
            tr_head.appendChild(doc.createTextNode(tabSymbols));

            Element updateForm = doc.createElement("form");
            updateForm.setAttribute("method","post");;
            updateForm.setAttribute("id","updForm" + it);

            lang.getParentNode().appendChild(updateForm);

        }
        lang.appendChild(tr_head);

        for (int row = 1; row < jsonArrayData.size(); row++){
            Element tr = doc.createElement("tr");
            firstJSONObj = jsonArrayData.get(row).toString();

            firstJSONObj = firstJSONObj.replaceAll("\"", "");
            firstJSONObj = firstJSONObj.substring(1,firstJSONObj.length());
            firstJSONObj = firstJSONObj.substring(0,firstJSONObj.length()-1);

            String[] dataFromTable = firstJSONObj.split(",");

            for(int col = 0;col < dataFromTable.length; col++){
                Element th = doc.createElement("th");

                Element inputElement = doc.createElement("input");
                inputElement.setAttribute("name",arr_col_names[col]);
                inputElement.setAttribute("value",dataFromTable[col]);
                inputElement.setAttribute("size","8");
                inputElement.setAttribute("form","updForm" + row);

                if(arr_col_names[col].contains("_ID")){
                    inputElement.setAttribute("readonly","readonly");
                }

                th.appendChild(inputElement);

                tr.appendChild(th);
                tr.appendChild(doc.createTextNode("\n"));
                tr.appendChild(doc.createTextNode(tabSymbols));
            }

            Element th_update = doc.createElement("th");
            Element inputUpdate = doc.createElement("input");
            Element inputDelete = doc.createElement("input");

            inputUpdate.setAttribute("type","submit");
            inputUpdate.setAttribute("form","updForm" + row);
            inputUpdate.setAttribute("name", "_isMethod");
            inputUpdate.setAttribute("value","put");

            inputDelete.setAttribute("type","submit");
            inputDelete.setAttribute("form","updForm" + row);
            inputDelete.setAttribute("name", "_isMethod");
            inputDelete.setAttribute("value","delete");

            th_update.appendChild(inputUpdate);
            th_update.appendChild(inputDelete);

            tr.appendChild(th_update);
            lang.appendChild(tr);
        }
    }
}
