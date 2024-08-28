package com.ghostreborn.akira

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.model.GraphQLRequest
import com.ghostreborn.akira.model.UserList
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val query = """
        query {
  MediaListCollection(userName:"GhostReborn" , type: ANIME) {
    lists {
      entries {
        media {
          title {
            userPreferred
          }
          coverImage {
            large
          }
        }
        progress
      }
    }
  }
}
    """.trimIndent()

        val request = GraphQLRequest(query)

        Constants.api.getUserList(request).enqueue(object :
            Callback<UserList> {
            override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                if (response.isSuccessful) {
                    Log.e("TAG", "onResponse: ${response.body()}")
                } else {
                    Log.e("TAG", "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<UserList>, t: Throwable) {
                Log.e("TAG", "onResponse: $t")
            }
        })


    }

    override fun onResume() {
        super.onResume()
        handleIntentData()
        checkLogin()
    }

    private fun checkLogin(){
        if (!AkiraUtils().checkLogin(this)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, LoginFragment())
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, HomeFragment())
                .commit()
        }
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AkiraUtils().getTokenAndUserID(code, this@MainActivity)
            }
        }
    }

}