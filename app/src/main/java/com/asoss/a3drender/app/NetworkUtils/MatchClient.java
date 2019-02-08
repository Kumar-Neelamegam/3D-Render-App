package com.asoss.a3drender.app.NetworkUtils;

import android.content.Context;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.asoss.a3drender.app.Adapters.RecyclerViewHorizontalListAdapter;
import com.asoss.a3drender.app.CoreModules.Constants;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.GlobalObjects.DataObjects;
import com.celites.androidexternalfilewriter.AppExternalFileWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

public class MatchClient {


    public Socket _socket;
    public Context ctx;
    public View view;
    public ArrayList<DataObjects> dataObjectsItems=new ArrayList<>();

    /*******************************************************************************************************************
     */

   //run the matching
    public ArrayList<DataObjects> requestMatching(String FileLocation, Context ctx, View view, BottomSheetDialog bottomDialog) {
        try {

            this.ctx=ctx;
            this.view=view;

            TextView tvMessage = bottomDialog.findViewById(R.id.tvMessage);

            //open connection
            //TODO use Ip and port from some settings
            _socket = new Socket(Constants.Server_IP, Constants.Server_Port);
            _socket.setSoTimeout(100000);

            //TODO send STATUS to gui ConnectionOpen
            tvMessage.setText("Processing Image..");
            File file = new File(FileLocation);//Passing image path
            Log.e("FilePath - Server : ", FileLocation);

            //TODO send STATUS to gui Sending Image
            //send image size json
            String imgSizeJson = String.format("{\"imagesize\": %d}", file.length());
            Constants.Logger("Send jsonImgSize: " + imgSizeJson);
            sendNullTerminatedString(imgSizeJson);

            try {
                Constants.Logger("Send image");
                byte[] img = new byte[(int) file.length()];
                InputStream fis = new FileInputStream(file);
                fis.read(img);
                fis.close();
                sendBinaryData(img);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                bottomDialog.dismiss();
                Constants.SnackBar(ctx,"Data processing internal error..", view, 2);
            }

            tvMessage.setText("Waiting for result..");
            //TODO send STATUS to gui Wait For Result
            Constants.Logger("Wait for result json");

            String resultJson = receiveNullTerminatedString();
            Constants.Logger("received: " + resultJson);

            //TODO send result to your Viewer
            //TODO send STATUS to gui Receive Stl
            Constants.Logger("Receive stl files");

            JSONArray arr = new JSONArray(resultJson);

            if(arr.length()==0)//show message to user
            {
                bottomDialog.dismiss();
                Constants.SnackBar(ctx,"No data found.. Try later..", view, 2);
            }else{

                dataObjectsItems = LoadDataList(ctx, arr);

                bottomDialog.dismiss();

                //File external writer
                AppExternalFileWriter appExternalFileWriter=new AppExternalFileWriter(ctx);

                //remove all the file before writing the new one
                File desfolder = new File(Environment.getExternalStorageDirectory().getPath()+"/"+ctx.getString(R.string.app_name)+"-stl");
                appExternalFileWriter.deleteDirectory(desfolder);


                for (int i = 0; i < arr.length(); i++)
                {
                    JSONObject res = arr.getJSONObject(i);
                    String fileName = res.getString("title");
                    int size = res.getInt("size");
                    int zippedSize = res.getInt("zippedSize");

                    Constants.Logger("receive File: " + fileName+ "(size:" + size + ",zippedSize:" + zippedSize + ")");

                    byte[] compressedData = receiveBinaryData(zippedSize);

                    Constants.Logger("decompress");

                    try {
                        byte[] data = CompressionUtils.decompress(compressedData);
                        Constants.Logger("decompressed size: " + data.length);

                        //TODO check that data.length == size OTherwise ERROR, This happens wehn zip and unzip algorthm do not match
                        //TODO send stl file to your viewer
                        // Files.write(new File(fileName).toPath(), data);

                        try {
                           //write the file to storage
                            appExternalFileWriter.writeDataToFile(fileName, data , false);
                        } catch (AppExternalFileWriter.ExternalFileWriterException e) {
                            e.printStackTrace();
                        }

                    } catch (DataFormatException exception) {

                        //TODO send STATUS to gui Failed Receive Stl files bad format
                        Constants.Logger("decompress DataFormatException");
                    } catch (IOException exception) {
                        //TODO send STATUS to gui Failed Receive Stl files input output errror
                        Constants.Logger("decompress IOException");
                    }

                }
            }



            _socket.close();

            Constants.Logger("Result Returned: "+ dataObjectsItems.toString());

            return dataObjectsItems;

        } catch (UnknownHostException exception) {
            // Output expected UnknownHostExceptions.
            exception.printStackTrace();
            //TODO send ERROR to gui Unknown Host while connection
            Constants.Logger("UnknownHostException");
            Constants.SnackBar(ctx,"UnknownHostException", view, 2);
        } catch (IOException exception) {
            //TODO send ERROR to Connection Error
            exception.printStackTrace();
            Constants.SnackBar(ctx,"IO Exception", view, 2);
        } catch (JSONException e) {
            e.printStackTrace();
            Constants.SnackBar(ctx,"JSON Exception", view, 2);
        }
        catch (Exception e)
        {
           e.printStackTrace();
            bottomDialog.dismiss();
            Constants.SnackBar(ctx,"Network Error..", view, 2);
        }

        return null;
    }





    /*******************************************************************************************************************
     */

    /**
     * Bind all the data from the server
     * @param ctx
     * @param arr
     * @return
     * @throws JSONException
     */
    private ArrayList<DataObjects> LoadDataList(Context ctx, JSONArray arr) throws JSONException {
        DataObjects dataObjects=new DataObjects();
        ArrayList<DataObjects> dataObjectsItems=new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {

            JSONObject res = arr.getJSONObject(i);
            String str_score            = res.getString("score");
            String str_title            = res.getString("title");
            String str_size             = res.getString("size");
            String str_FileLocation     = Environment.getExternalStorageDirectory().getPath()+"/"+ctx.getString(R.string.app_name)+"-stl/"+str_title;

            dataObjects=new DataObjects();
            dataObjects.setId(i);
            dataObjects.setScore(str_score);
            dataObjects.setTitle(str_title);
            dataObjects.setSize(str_size);
            dataObjects.setFileLocation(str_FileLocation);
            dataObjectsItems.add(dataObjects);
        }

        return dataObjectsItems;
    }




    /*******************************************************************************************************************
     */

    public void sendNullTerminatedString(String str) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(_socket.getOutputStream(), "UTF-8");
            osw.write(str);
            //send null byte
            osw.write(0);
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /*******************************************************************************************************************
     */

    public void sendBinaryData(byte[] data)  {
        try {
            DataOutputStream dos = new DataOutputStream(_socket.getOutputStream());
            dos.write(data, 0, data.length);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*******************************************************************************************************************
     */

    public String receiveNullTerminatedString() throws IOException {
        byte[] buffer = new byte[1024];

        InputStream sis = _socket.getInputStream();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();


        while (true) {
            int bytesRead = sis.read(buffer);

            if (bytesRead <= -1) {
                //error nothin to read handle this
                break;
            } else if (buffer[bytesRead - 1] == 0) {
                //null termination of string
                //null termination is not appended to string
                resultBytes.write(buffer, 0, bytesRead - 1);
                break;
            } else {
                resultBytes.write(buffer, 0, bytesRead);
            }
        }
        String str = new String(resultBytes.toByteArray(), "UTF-8");
        return str;

    }
    /*******************************************************************************************************************
     */

    public byte[] receiveBinaryData(int numBytes) throws IOException {
        byte[] buffer = new byte[1024];

        InputStream sis = _socket.getInputStream();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();

        while (true) {
            int bytesRead = sis.read(buffer);

            if (bytesRead <= -1) {
                //error nothin to read handle this
                break;
            } else {
                resultBytes.write(buffer, 0, bytesRead);
            }

            if (resultBytes.size() == numBytes) {
                //we received all bytes
                break;
            }

        }
        return resultBytes.toByteArray();

    }


}//end
