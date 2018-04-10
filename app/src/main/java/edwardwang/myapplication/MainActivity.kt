package edwardwang.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.Charset
import java.nio.file.Paths
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    var isConnected:Boolean = false
    var testFileKey:ByteArray = "000102030405060708090A0B0C0D0E0F".toByteArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //widgets
        //setUpWidgets()
        cipherTest()
    }

    fun setUpWidgets()
    {
        val ipAddress = findViewById<EditText>(R.id.ipAddressInput)
        val port = findViewById<EditText>(R.id.portInput)
        val connToServer:Button = findViewById<Button>(R.id.connToServerButton)
        connToServer.setOnClickListener {
            val serverInfo:String = "IPAddress: "+ipAddress + " \tPort:" + port
            Log.i("ConnToServer",serverInfo)
            connectToServer(ipAddress.text.toString(), port.text.toString().toInt())
            //val intent = Intent(this, StreamDisplay::class.java);
            //startActivity(intent);
        }
        val disconnectButton = findViewById<Button>(R.id.disconnectButton)
        disconnectButton.setOnClickListener {

        }
    }

    fun cipherTest()
    {
        try
        {
            val serverOutputTextView:TextView = findViewById<TextView>(R.id.serverOutput)
            //serverOutputTextView.text = filesDir.toString()
            //System.out.println(filesDir.toString())
            //val inputStream: InputStream = File(filesDir.toString() + "/testEncryptedAES128.txt").inputStream() as InputStream
            //val inputStringTest = inputStream.bufferedReader().use { it.readText() }
            val encrypt = "����^��h�0V&� O��Vwz�m�l,�I c".toByteArray()
            val iv:ByteArray = ByteArray(16)
            val decryptedMessage:String = decrypt(iv, testFileKey, encrypt).toString()

            System.out.println("Decrypted: "+decryptedMessage)
            //serverOutputTextView.text = decryptedMessage
        }catch (e: IOException)
        {
            e.printStackTrace()
        }
    }

    /**
     * Decrypt AES 128 block
     *
     * test file: testEncryptAES128.jpg
     * test key: 000102030405060708090A0B0C0D0E0F
     *
     * @param ivBytes         : initialization vector - 16 bytes for a 128-byte block
     * @param secretKeyBytes  : secret key
     * @param encrypted       : encrypted block
     * */
    fun decrypt(ivBytes:ByteArray, secretKeyBytes:ByteArray, encrypted:ByteArray):ByteArray
    {
        var initializationVector:AlgorithmParameterSpec = IvParameterSpec(ivBytes)
        var secretKeySpec:SecretKeySpec = SecretKeySpec(secretKeyBytes, "AES")
        var cipher:Cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector)
        return cipher.doFinal(encrypted)
    }

    fun connectToServer(ipAddress:String, port:Int)
    {
        Thread({
            val servAddress = InetAddress.getByName(ipAddress)
            val socket = Socket(servAddress, port)
            val serverOutputTextView:TextView = findViewById<TextView>(R.id.serverOutput)
            try
            {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                runOnUiThread({
                    isConnected = true
                    serverOutputTextView.text = "Connected To Server"
                })

                Log.d("ConnectToServer","Begin Reading")

                var txt = ""
                while(true)
                {
                    if(input.ready())
                    {
                        Log.d("ConnectToServer","Ready to read from server.")

                        //saveStream()

                        txt = input.readLine()
                        if(txt != null)
                        {
                            runOnUiThread({
                                serverOutputTextView.text = txt
                            })
                            Log.d("ConnectToServer","Txt: " + txt)
                        }else{
                            Log.d("ConnectToServer","Text is null")
                            break
                        }
                    }
                }

                Log.d("ConnectToServer","Disconnect From Server")
                socket.close()

                runOnUiThread({
                    serverOutputTextView.text = "Disconnected To Server"
                    isConnected = false
                })
            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }).start()
    }

    fun saveStream()
    {

    }

}