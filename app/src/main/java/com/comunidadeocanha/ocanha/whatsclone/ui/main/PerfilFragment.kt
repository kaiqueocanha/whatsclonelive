package com.comunidadeocanha.ocanha.whatsclone.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.comunidadeocanha.ocanha.whatsclone.InicioActivity
import com.comunidadeocanha.ocanha.whatsclone.MainActivity
import com.comunidadeocanha.ocanha.whatsclone.R
import com.comunidadeocanha.ocanha.whatsclone.VisualizarImagemActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.view.*

class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewPerfil = inflater.inflate(R.layout.fragment_perfil, container, false)

        carregaImagemUsuario(viewPerfil)

        carregaInfoUsuario(viewPerfil)

        viewPerfil.cvVisualizarFoto.setOnClickListener {
            visualizarFoto()
        }

        viewPerfil.btnSair.setOnClickListener {
            sair()
        }

        viewPerfil.btnExcluir.setOnClickListener {

            excluirConta()

        }

        return viewPerfil
    }

    private fun excluirConta() {
        
    }

    private fun sair() {
        FirebaseAuth.getInstance().signOut()
        activity?.startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun visualizarFoto() {

        val intent = Intent(activity, VisualizarImagemActivity::class.java)

        intent.putExtra("TITLE", "Foto de perfil")
        intent.putExtra("URL", (activity as InicioActivity).usuarioLogado.getString("foto"))

        activity?.startActivity(intent)

    }

    private fun carregaImagemUsuario(view: View?) {

        Picasso.get()
            .load((activity as InicioActivity).usuarioLogado.getString("foto"))
            .placeholder(R.drawable.profile_default)
            .error(R.drawable.profile_default)
            .into(view?.imgPerfil)

    }

    private fun carregaInfoUsuario(view: View?) {

        view?.tvNome?.text = (activity as InicioActivity).usuarioLogado.getString("nome")
        view?.tvEmail?.text = (activity as InicioActivity).usuarioLogado.getString("email")

    }

    companion object {
        @JvmStatic
        fun newInstance() = PerfilFragment()
    }
}