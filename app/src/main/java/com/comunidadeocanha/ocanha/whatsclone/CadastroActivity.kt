package com.comunidadeocanha.ocanha.whatsclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro.*

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_cadastro)

        this.auth = FirebaseAuth.getInstance()

        tbCadastro.setNavigationOnClickListener {
            abreMainActivity()
        }

        btnCadastro.setOnClickListener {

            cadastro()

        }

    }

    private fun cadastro() {

        if (validacaoCadastro()) {

            cadastroCarregando(true)

            auth.createUserWithEmailAndPassword(
                edtEmailCadastro.text.toString(),
                edtSenhaCadastro.text.toString()
            ).addOnCompleteListener { cadastro ->

                if (cadastro.isSuccessful) {

                    auth.currentUser!!.sendEmailVerification().addOnCompleteListener { envioEmail ->

                        if (envioEmail.isSuccessful) {

                            sucessoCadastro()

                        } else {

                            auth.currentUser!!.delete().addOnCompleteListener {

                                erroCadastro(envioEmail.exception?.message!!)
                                cadastroCarregando(false)

                            }

                        }

                    }

                } else {

                    erroCadastro("Falha ao realizar operação. Motivo: ${cadastro.exception?.message}")
                    cadastroCarregando(false)

                }

            }

        } else {

            erroCadastro("Verifique os campos incorretos")

        }

    }

    private fun sucessoCadastro() {
        abreMainActivity()
    }

    private fun cadastroCarregando(flag: Boolean) {

        edtNomeCadastro.isEnabled = !flag
        edtEmailCadastro.isEnabled = !flag
        edtSenhaCadastro.isEnabled = !flag

        btnCadastro.isEnabled = !flag

        btnCadastro.text = if (flag) "Carregando..." else "Cadastrar"

        pgCadastro.visibility = if (flag) View.VISIBLE else View.INVISIBLE

    }

    private fun erroCadastro(s: String) {

        Toast.makeText(
            this,
            s,
            Toast.LENGTH_LONG
        ).show()

    }

    private fun validacaoCadastro(): Boolean {

        if (edtNomeCadastro.text.isBlank() || edtNomeCadastro.text.isEmpty()) {
            edtNomeCadastro.error = "Nome de usuário incorreto."
            edtNomeCadastro.requestFocus()
            return false
        }

        if (edtEmailCadastro.text.isBlank()
            || edtEmailCadastro.text.isEmpty()
            || !android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmailCadastro.text).matches()
        ) {
            edtEmailCadastro.error = "Endereço de e-mail incorreto."
            edtEmailCadastro.requestFocus()
            return false
        }

        if (edtSenhaCadastro.text.isBlank() || edtSenhaCadastro.text.isEmpty()) {
            edtSenhaCadastro.error = "Senha de acesso invalida."
            edtSenhaCadastro.requestFocus()
            return false
        }

        if (edtSenhaCadastro.text.length < 6) {
            edtSenhaCadastro.error = "Senha de acesso deve ter no mínimo 6 caracteres."
            edtSenhaCadastro.requestFocus()
            return false
        }

        return true

    }

    private fun abreMainActivity() {

        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }
}