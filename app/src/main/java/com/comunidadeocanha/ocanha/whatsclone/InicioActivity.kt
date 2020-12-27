package com.comunidadeocanha.ocanha.whatsclone

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.comunidadeocanha.ocanha.whatsclone.ui.main.SectionsPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_inicio.*
import org.w3c.dom.Document

class InicioActivity : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()

    lateinit var usuarioLogado: DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_inicio)

        loadUsuario()

        fab.setOnClickListener { view ->
            iniciarConversar()
        }
    }

    private fun iniciarConversar() {
        TODO("Not yet implemented")
    }

    private fun loadUsuario() {

        this.db.collection("usuarios")
            .document(this.auth.currentUser!!.uid)
            .addSnapshotListener { usuario, _ ->

                this.usuarioLogado = usuario!!

            }

        this.db.collection("usuarios")
            .document(this.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { resultado ->

                this.usuarioLogado = resultado

                iniciarInterface()

            }
            .addOnFailureListener { excecao ->

                Toast.makeText(
                    this,
                    "Falha ao realziar a operação. Motivo: " + excecao.message,
                    Toast.LENGTH_LONG
                ).show()

            }

    }

    private fun iniciarInterface() {

        pgInicio.visibility = View.INVISIBLE
        fab.visibility = View.VISIBLE

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

    }

}