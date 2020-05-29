import org.json.simple.JSONArray;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class RestServerHandler extends Thread {
    private Socket socket;

    private String method;

    private String requestPayload;

    private DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    RestServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            parseRequest(input);
            switch (method){
                case "GET":{
                    var type = "application/json";
                    var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                    this.sendHeader(output, 200, "OK", type, fileBytes.length);

                    output.write(fileBytes);
                    break;
                }
                case "POST":{
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }
                    createNewRecordInTheDB(keyValuePair);

                    var type = "application/json";
                    var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                    this.sendHeader(output, 200, "OK", type, fileBytes.length);

                    output.write(fileBytes);

                    break;
                }
                case "PUT":{
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }
                    updateRecordInTheDB(keyValuePair);

                    var type = "application/json";
                    var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                    this.sendHeader(output, 200, "OK", type, fileBytes.length);

                    output.write(fileBytes);

                    break;
                }
                case "DELETE":{
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }
                    deleteRecordInTheDB(keyValuePair);

                    var type = "application/json";
                    var fileBytes =  getAllInfoFromDB().toString().getBytes("utf-8");
                    this.sendHeader(output, 200, "OK", type, fileBytes.length);

                    output.write(fileBytes);


                    break;
                }
                default:{
                    System.err.println("WE TAKE unidentified METHOD");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long lenght) {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Date: %s%n", DTF.format(LocalDateTime.now())); ////////////////
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", lenght);
    }

    private void parseRequest(InputStream input) throws IOException {
        InputStreamReader isReader = new InputStreamReader(input);
        BufferedReader br = new BufferedReader(isReader);

        String firstLine = br.readLine();
        method = firstLine.split(" ")[0];
        System.out.println("firstLine = " + firstLine);

        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){
           System.out.println(headerLine);
        }

        StringBuilder payload = new StringBuilder();
        while(br.ready()){
            payload.append((char) br.read());
        }

        requestPayload = payload.toString();
    }


    private ArrayList<ArrayList<String>> parseRequestPayload(){
        ArrayList<ArrayList<String>> HalvesOfParamenter = new ArrayList<ArrayList<String>>();
        for(int i = 0;i < 2; i++) {
            HalvesOfParamenter.add(new ArrayList<String>());
        }
        int i = 0;
        for (String retval : requestPayload.split("&")) {
            HalvesOfParamenter.get(0).add(retval.split("=",2)[0]);
            HalvesOfParamenter.get(1).add(retval.split("=",2)[1].replaceAll("\r\n",""));
            i++;
        }
        return HalvesOfParamenter;
    }


    public static JSONArray getAllInfoFromDB(){
        return configurateJsonArray(Database.getAllInfoFromDatabase());
    }

    public static JSONArray createNewRecordInTheDB( ArrayList<ArrayList<String>> keyValuePair){
        Database.createNewRecordInTheDatabase(keyValuePair);
        return configurateJsonArray(Database.getAllInfoFromDatabase());
    }

    public static JSONArray updateRecordInTheDB(ArrayList<ArrayList<String>> keyValuePair){
        Database.updateRecordInTheDatabase(keyValuePair);
        return configurateJsonArray(Database.getAllInfoFromDatabase());
    }

    public static JSONArray deleteRecordInTheDB(ArrayList<ArrayList<String>> keyValuePair){
        Database.deleteRecordInTheDatabase(keyValuePair);
        return configurateJsonArray(Database.getAllInfoFromDatabase());

    }

    private static JSONArray configurateJsonArray(DatabaseResponse databaseResponse){
        JSONArray result = new JSONArray();
        JSONArray list = new JSONArray();

        for (int i = 0; i < databaseResponse.arr_col_names.length; i ++){
           list.add(databaseResponse.arr_col_names[i]);
        }
        result.add(list);

        for (int row = 0; row < databaseResponse.fullDataFromSet[0].length; row++) {
            JSONArray list2 = new JSONArray();
            for(int col = 0;col < databaseResponse.fullDataFromSet.length; col++){
                list2.add(databaseResponse.fullDataFromSet[col][row].replaceAll("\r\n",""));
            }
            result.add(list2);
        }

        return result;
    }
}
