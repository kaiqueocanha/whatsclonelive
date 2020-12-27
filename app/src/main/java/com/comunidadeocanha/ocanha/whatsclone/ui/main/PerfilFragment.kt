package com.comunidadeocanha.ocanha.whatsclone.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.comunidadeocanha.ocanha.whatsclone.InicioActivity
import com.comunidadeocanha.ocanha.whatsclone.MainActivity
import com.comunidadeocanha.ocanha.whatsclone.R
import com.comunidadeocanha.ocanha.whatsclone.VisualizarImagemActivity
import com.comunidadeocanha.ocanha.whatsclone.utils.Constanst
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cadastro.*
import kotlinx.android.synthetic.main.bottom_sheet_altera_foto.view.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.btnSalvar
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.btnCancelar
import kotlinx.android.synthetic.main.bottom_sheet_edit_password.view.*
import kotlinx.android.synthetic.main.fragment_perfil.view.*
import java.io.ByteArrayOutputStream

class PerfilFragment : Fragment() {

    val REQUISICAO_FOTO_GALERIA = 101
    val REQUISICAO_FOTO_CAMERA = 102

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewPerfil = inflater.inflate(R.layout.fragment_perfil, container, false)

        carregaImagemUsuario(viewPerfil)
        carregaInfoUsuario(viewPerfil)

        (activity as InicioActivity).db.collection("usuarios")
            .document((activity as InicioActivity).auth.currentUser!!.uid)
            .addSnapshotListener { _, _ ->
                carregaImagemUsuario(viewPerfil)
                carregaInfoUsuario(viewPerfil)
            }

        viewPerfil.cvVisualizarFoto.setOnClickListener {
            visualizarFoto()
        }

        viewPerfil.imgBtnEditarNome.setOnClickListener {

            editarNomeUsario()

        }

        viewPerfil.imgBtnEditarSenha.setOnClickListener {

            editarSenhaUsuario()

        }

        viewPerfil.cvAlterarFoto.setOnClickListener {

            alterarFoto()

        }

        viewPerfil.btnSair.setOnClickListener {
            sair()
        }

        viewPerfil.btnExcluir.setOnClickListener {

            excluirConta()

        }

        return viewPerfil
    }

    private fun alterarFoto() {

        val btmSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_altera_foto, null)
        val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(btmSheetLayout)

        btmSheetLayout.imgBtnGaleria.setOnClickListener {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Selecionar foto"),
                REQUISICAO_FOTO_GALERIA
            )
            dialog.dismiss()

        }

        btmSheetLayout.imgBtnCamera.setOnClickListener {

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUISICAO_FOTO_CAMERA)
                }
            }
            dialog.dismiss()

        }

        btmSheetLayout.imgBtnExcluirFoto.setOnClickListener {

            updateImagemPerfil(Constanst.URL_DEFAULT_PROFILE_PHOTO)

            dialog.dismiss()

        }

        dialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUISICAO_FOTO_GALERIA || requestCode == REQUISICAO_FOTO_CAMERA) {

            val referenciaStorage = FirebaseStorage.getInstance().reference

            val referenciaArquivo = referenciaStorage.child(
                "imagens_perfil/"
                        + (activity as InicioActivity).auth.currentUser?.uid
                        + "/"
                        + System.currentTimeMillis().toString()
            )

            when (requestCode) {

                REQUISICAO_FOTO_CAMERA -> {

                    if (resultCode == RESULT_OK) {

                        val imagemBitmap = data?.extras?.get("data") as Bitmap
                        val baos = ByteArrayOutputStream()
                        imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val arquivo = baos.toByteArray()

                        val tarefaUploadArquivo = referenciaArquivo.putBytes(arquivo)
                        tarefaUploadArquivo.addOnCompleteListener { upload ->
                            trataUploadFoto(upload, referenciaArquivo)
                        }
                    }

                }
                REQUISICAO_FOTO_GALERIA -> {
                    if (data?.data != null) {

                        val arquivo = data.data

                        val tarefaUploadArquivo = referenciaArquivo.putFile(arquivo!!)
                        tarefaUploadArquivo.addOnCompleteListener { upload ->

                            trataUploadFoto(upload, referenciaArquivo)

                        }
                    }
                }
            }

        }

    }

    private fun trataUploadFoto(
        upload: Task<UploadTask.TaskSnapshot>,
        referenciaArquivo: StorageReference
    ) {

        if (upload.isSuccessful) {

            referenciaArquivo.downloadUrl.addOnSuccessListener { uri ->

                updateImagemPerfil(uri.toString())

            }.addOnFailureListener { excecao ->

                Toast.makeText(
                    activity,
                    "Fala ao realizar a operação. Motivo: " + excecao.message,
                    Toast.LENGTH_LONG
                ).show()

            }

        } else {

            Toast.makeText(
                activity,
                "Falha ao realizar a operação. Motivo: " + upload.exception?.message,
                Toast.LENGTH_LONG
            ).show()

        }

    }

    private fun updateImagemPerfil(foto: String) {

        (activity as InicioActivity).db.collection("usuarios")
            .document((activity as InicioActivity).auth.currentUser!!.uid)
            .update("foto", foto)
            .addOnFailureListener { excecao ->

                Toast.makeText(
                    activity,
                    "Falha ao realizar a operação. Motivo: " + excecao.message,
                    Toast.LENGTH_LONG
                ).show()

            }

    }

    private fun editarSenhaUsuario() {

        val btmSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_edit_password, null)
        val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(btmSheetLayout)

        btmSheetLayout.btnCancelar.setOnClickListener {
            dialog.cancel()
        }

        btmSheetLayout.btnSalvar.setOnClickListener {

            salvarSenhaUsuario(btmSheetLayout, dialog)

        }

        dialog.show()

    }

    private fun salvarSenhaUsuario(btmSheetLayout: View?, dialog: BottomSheetDialog) {

        if (validaSenha(btmSheetLayout)) {

            carregandoAlterarSenha(btmSheetLayout, true)

            val credencial = EmailAuthProvider.getCredential(
                (activity as InicioActivity).auth.currentUser?.email.toString(),
                btmSheetLayout?.edtSenhaAtual?.text.toString()
            )

            (activity as InicioActivity).auth.currentUser?.reauthenticate(credencial)
                ?.addOnCompleteListener { reautenticacao ->

                    if (reautenticacao.isSuccessful) {

                        (activity as InicioActivity).auth.currentUser
                            ?.updatePassword(btmSheetLayout?.edtSenhaNova?.text.toString())
                            ?.addOnCompleteListener { atualizacaoSenha ->

                                if (atualizacaoSenha.isSuccessful) {

                                    Toast.makeText(
                                        activity,
                                        "Senha alterada com sucesso!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    dialog.dismiss()

                                } else {

                                    Toast.makeText(
                                        activity,
                                        "Erro ao atualizar senha. " + atualizacaoSenha.exception?.message,
                                        Toast.LENGTH_LONG
                                    ).show()

                                    carregandoAlterarSenha(btmSheetLayout, false)

                                }

                            }

                    } else {

                        Toast.makeText(
                            activity,
                            "Senha atual incorreta. " + reautenticacao.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()

                        carregandoAlterarSenha(btmSheetLayout, false)

                    }

                }


        } else {

            Toast.makeText(
                activity,
                "Verifique os campos incorretos.",
                Toast.LENGTH_LONG
            ).show()

        }

    }

    private fun carregandoAlterarSenha(btmSheetLayout: View?, flag: Boolean) {

        btmSheetLayout?.edtSenhaAtual?.isEnabled = !flag
        btmSheetLayout?.edtSenhaNova?.isEnabled = !flag
        btmSheetLayout?.edtSenhaNovaConfirmacao?.isEnabled = !flag

        btmSheetLayout?.btnSalvar?.isEnabled = !flag
        btmSheetLayout?.btnCancelar?.isEnabled = !flag


    }

    private fun validaSenha(btmSheetLayout: View?): Boolean {

        val edtSenhaNova = btmSheetLayout?.edtSenhaNova

        val senhaNova = edtSenhaNova?.text.toString()

        if (senhaNova.isBlank() || senhaNova.isEmpty()) {
            edtSenhaNova?.error = "Senha de acesso invalida."
            edtSenhaNova?.requestFocus()
            return false
        }

        if (senhaNova.length < 6) {
            edtSenhaNova?.error = "Senha de acesso deve ter no mínimo 6 caracteres."
            edtSenhaNova?.requestFocus()
            return false
        }

        val edtSenhaNovaConfirmacao = btmSheetLayout?.edtSenhaNovaConfirmacao

        if (edtSenhaNova?.text.toString() != edtSenhaNovaConfirmacao?.text.toString()) {
            edtSenhaNovaConfirmacao?.error = "A confirmação da nova senha não está correta."
            edtSenhaNovaConfirmacao?.requestFocus()
            return false
        }

        return true

    }

    private fun editarNomeUsario() {

        val btmSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(btmSheetLayout)

        btmSheetLayout.edtNome.setText(((activity as InicioActivity).usuarioLogado.getString("nome")))
        btmSheetLayout.edtNome.selectAll()

        btmSheetLayout.btnCancelar.setOnClickListener {
            dialog.cancel()
        }

        btmSheetLayout.btnSalvar.setOnClickListener {

            salvarNomeUsuario(btmSheetLayout, dialog)

        }

        dialog.show()

    }

    private fun salvarNomeUsuario(btmSheetDialog: View, dialog: BottomSheetDialog) {

        btmSheetDialog.edtNome.isEnabled = false
        btmSheetDialog.btnSalvar.isEnabled = false
        btmSheetDialog.btnCancelar.isEnabled = false

        (activity as InicioActivity).db.collection("usuarios")
            .document((activity as InicioActivity).auth.currentUser!!.uid)
            .update("nome", btmSheetDialog.edtNome.text.toString())
            .addOnCompleteListener { update ->

                if (update.isSuccessful) {
                    dialog.dismiss()
                } else {

                    Toast.makeText(
                        activity,
                        "Falha ao atualizar o nome do usuário. " + update.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()

                    btmSheetDialog.edtNome.isEnabled = true
                    btmSheetDialog.btnSalvar.isEnabled = true
                    btmSheetDialog.btnCancelar.isEnabled = true

                }

            }
    }

    private fun excluirConta() {

        val btmSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_excluir_conta, null)
        val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(btmSheetLayout)

        btmSheetLayout.btnCancelar.setOnClickListener {
            dialog.cancel()
        }

        btmSheetLayout.btnSalvar.setOnClickListener {

            salvarExcluirConta(btmSheetLayout)

        }

        dialog.show()

    }

    private fun salvarExcluirConta(btmSheetLayout: View?) {

        btmSheetLayout?.edtSenhaAtual?.isEnabled = false

        val credencial = EmailAuthProvider.getCredential(
            (activity as InicioActivity).auth.currentUser?.email.toString(),
            btmSheetLayout?.edtSenhaAtual?.text.toString()
        )

        (activity as InicioActivity).auth.currentUser?.reauthenticate(credencial)
            ?.addOnCompleteListener { reautenticacao ->

                if (reautenticacao.isSuccessful) {

                    (activity as InicioActivity).auth.currentUser
                        ?.delete()
                        ?.addOnCompleteListener { exclusaoUsuario ->

                            if (exclusaoUsuario.isSuccessful) {

                                activity?.startActivity(Intent(activity, MainActivity::class.java))
                                activity?.finish()

                            } else {

                                Toast.makeText(
                                    activity,
                                    "Falha ao realizar a operação. Motivo: " + exclusaoUsuario.exception?.message,
                                    Toast.LENGTH_LONG
                                ).show()

                            }

                        }

                } else {

                    Toast.makeText(
                        activity,
                        "Senha atual incorreta. " + reautenticacao.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()

                    btmSheetLayout?.edtSenhaAtual?.isEnabled = true

                }

            }


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