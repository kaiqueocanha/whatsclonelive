package com.comunidadeocanha.ocanha.whatsclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnCadastro
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {

            if (!user.isEmailVerified) {
                abreVerifiqueEmailActivity()
            } else {
                abreInicioActivity()
            }

        }

        btnCadastro.setOnClickListener {

            abreCadastroActivity()

        }

        btnEntrar.setOnClickListener {

            if (validacaoLogin()) {

                loginCarregando(true)

                auth.signInWithEmailAndPassword(
                    edtEmailLogin.text.toString(),
                    edtSenhaLogin.text.toString()
                ).addOnCompleteListener { login ->

                    if (login.isSuccessful) {

                        abreInicioActivity()

                    } else {

                        Toast.makeText(
                            this,
                            "Autenticação falhou. Verifique seu e-mai e senha.",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                }

            } else {
                Toast.makeText(this, "Verifique os campos incorretos", Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun loginCarregando(flag: Boolean) {

        edtEmailLogin.isEnabled = !flag
        edtSenhaLogin.isEnabled = !flag

        btnCadastro.isEnabled = !flag
        btnEntrar.isEnabled = !flag

        btnEntrar.text = if (flag) "Carregando..." else "Entrar"
        pgLogin.visibility = if (flag) View.VISIBLE else View.INVISIBLE

    }

    private fun validacaoLogin(): Boolean {

        if (edtEmailLogin.text.isBlank()
            || edtEmailLogin.text.isEmpty()
            || !android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmailLogin.text).matches()
        ) {
            edtEmailLogin.error = "Endereço de e-mail incorreto."
            edtEmailLogin.requestFocus()
            return false
        }

        if (edtSenhaLogin.text.isBlank() || edtSenhaLogin.text.isEmpty()) {
            edtSenhaLogin.error = "Senha de acesso invalida."
            edtSenhaLogin.requestFocus()
            return false
        }

        if (edtSenhaLogin.text.length < 6) {
            edtSenhaLogin.error = "Senha de acesso deve ter no mínimo 6 caracteres."
            edtSenhaLogin.requestFocus()
            return false
        }

        return true
    }

    private fun abreInicioActivity() {
        startActivity(Intent(this, InicioActivity::class.java))
        finish()
    }

    private fun abreVerifiqueEmailActivity() {
        startActivity(Intent(this, VerifiqueEmailActivity::class.java))
        finish()
    }

    private fun abreCadastroActivity() {

        startActivity(Intent(this, CadastroActivity::class.java))
        finish()

    }

}