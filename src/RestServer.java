import org.json.simple.JSONArray;

import java.util.ArrayList;

public class RestServer {

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
                list2.add(databaseResponse.fullDataFromSet[col][row]);
            }
            result.add(list2);
        }

        return result;
    }
}
