package br.edu.infnet.at_anotacoes

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(
    view: View,
    msg: String,
    laength: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
){
    val snackbar = Snackbar.make(view, msg, laength)
    if(actionMessage != null){
        snackbar.setAction(actionMessage){
            action(this)
        }.show()

    }    else{
        snackbar.show()
    }


}fun AppCompatActivity.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}