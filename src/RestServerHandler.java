import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class RestServerHandler extends Thread {
    private Socket socket;

    private String method;

    private String requestPayload;

    RestServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            parseRequest(input);
            switch (method){
                case "GET":{
                    System.out.println("WE TAKE GET METHOD");
                    JSONArray result = getAllInfoFromDB();
                    output.write(result.toString().getBytes());
                    break;
                }
                case "POST":{
                    System.out.println("WE TAKE POST METHOD");
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                        //keyValuePair.get(1).get(i) = keyValuePair.get(1).get(i).replaceAll("\r\n", "");
                        System.out.println(keyValuePair.get(1).get(i) instanceof String);
                    }
                    createNewRecordInTheDB(keyValuePair);

                    JSONArray result = getAllInfoFromDB();

                    System.out.println("Result POST : " );
                    System.out.println(result);
                    output.write(result.toString().getBytes());

                    break;
                }
                case "PUT":{
                    System.out.println("WE TAKE PUT METHOD");
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }
                    updateRecordInTheDB(keyValuePair);

                    JSONArray result = getAllInfoFromDB();
                    // JSONArray result = getAllInfoFromDB();
                    System.out.println("Result: " );
                    System.out.println(result);
                    output.write(result.toString().getBytes());
                    // output.write(getAllInfoFromDB().toString().getBytes());
                    break;
                }
                case "DELETE":{
                    System.out.println("WE TAKE DELETE METHOD");
                    ArrayList<ArrayList<String>> keyValuePair = parseRequestPayload();
                    for(int i = 0; i < keyValuePair.get(0).size(); i++){
                        System.out.println("fist = " + keyValuePair.get(0).get(i));
                        System.out.println("second = " + keyValuePair.get(1).get(i));
                    }
                    deleteRecordInTheDB(keyValuePair);
                    JSONArray result = getAllInfoFromDB();

                    output.write(result.toString().getBytes());


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
        //System.out.println("method = " + method);
        // System.out.println("requestPayload = " + requestPayload);

        //requestPayload = br.readLine();
        //System.out.println("requestPayload = " + requestPayload);
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
        System.out.println("NNENEN");

        for (int i = 0; i < databaseResponse.arr_col_names.length; i ++){
           list.add(databaseResponse.arr_col_names[i]);
        }
        result.add(list);

        for (int row = 0; row < databaseResponse.fullDataFromSet[0].length; row++) {
            JSONArray list2 = new JSONArray();
            for(int col = 0;col < databaseResponse.fullDataFromSet.length; col++){
                System.out.println("Добавляем вот это" +databaseResponse.fullDataFromSet[col][row] );
                list2.add(databaseResponse.fullDataFromSet[col][row].replaceAll("\r\n",""));
            }
            result.add(list2);
        }

        return result;
    }
}
