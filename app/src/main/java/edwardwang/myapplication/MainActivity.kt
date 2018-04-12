package edwardwang.myapplication

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
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    var isConnected:Boolean = false
    //val testFileKey = "000102030405060708090A0B0C0D0E0F".toByteArray()

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
            //break encrypted file into 16 byte/128 bit blocks
            val inputStream:InputStream = resources.openRawResource(R.raw.secret3)
            val readInEncrypt = inputStream.readBytes(16)
            System.out.println("Size: "+readInEncrypt.size)
            //get array of byte array blocks
            val numOfBlocks:Int = readInEncrypt.size/4
            var encryptedBlocks:Array<ByteArray> = getEncryptedBlocksByteArray(numOfBlocks,
                    readInEncrypt)

            //get key
            val key = ByteArray(16)
            //secret2
            //00000001 00000000 00000000
            key[3] = 1.toByte()

            //set initialization vector
            val iv = ByteArray(16)

            //AES decrypts at 16 bytes at a given time
            val decryptedByteArray = getDecryptedByteArray(numOfBlocks, encryptedBlocks, iv, key)

            //convert char to ascii
            val charset = Charsets.US_ASCII
            serverOutputTextView.text = correctedDecryptedByteArray.toString(charset)
        }catch (e: IOException)
        {
            e.printStackTrace()
        }
    }

    fun getDecryptedByteArray(numOfBlocks: Int,encryptedBlocks:Array<ByteArray>, iv:ByteArray, key:ByteArray):ByteArray
    {
        var decryptedByteArray = ByteArray(numOfBlocks)
        var decryptedBlock = ByteArray(16)
        for(i in 0..numOfBlocks-1)
        {
            for(j in 0..15)
            {
                if(j==0) decryptedByteArray[i*16] = decrypt(iv, key, encryptedBlocks[i])
                else block[j] = byteArray[i*16 + j]
            }
        }
        return decryptedByteArray
    }

    fun getEncryptedBlocksByteArray(numOfBlocks:Int, byteArray:ByteArray):Array<ByteArray>
    {
        var encryptedBlocks = Array<ByteArray>(numOfBlocks,{ ByteArray(16) })
        for(i in 0..numOfBlocks)
        {
            val block = ByteArray(16)
            for(j in 0..15)
            {
                if(j==0) block[j] = byteArray[i * 16]
                else block[j] = byteArray[i*16 + j]
            }
            encryptedBlocks[i] = reverseBlockOrder(block)
        }
        return encryptedBlocks
    }

    fun reverseBlockOrder(byteArray: ByteArray) : ByteArray
    {
        var reversedByteArray = ByteArray(16)
        for(i in 0..15) reversedByteArray[i] = byteArray[15-i]
        return reversedByteArray
    }

    /**
     * Decrypt AES 128-bits block in 16 bytes blocks
     *
     * test file: testEncryptAES128.jpg
     * test key: 0x000102030405060708090A0B0C0D0E0F
     *
     * @param ivBytes         : initialization vector - 16 bytes for a 128-byte block
     * @param secretKeyBytes  : secret key            - 16 bytes
     * @param encrypted       : encrypted block
     * */
    fun decrypt(ivBytes:ByteArray, secretKeyBytes:ByteArray, encrypted:ByteArray):ByteArray
    {
        val initializationVector = IvParameterSpec(ivBytes)
        val secretKeySpec = SecretKeySpec(secretKeyBytes, "AES_128")
        val cipher:Cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector)
        try{
            return reverseBlockOrder(cipher.doFinal(encrypted))
        }catch (e: IllegalBlockSizeException)
        {
            System.out.println("Illegal Block Size Exception")
            e.printStackTrace()
            var newEncrypted = ByteArray(16)
            for(i in 0..encrypted.size-1) newEncrypted[i] = encrypted[i]
            if(encrypted.size < 15) for(i in encrypted.size..15) newEncrypted[i] = 0
            return reverseBlockOrder(cipher.doFinal(newEncrypted))
        }
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