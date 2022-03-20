package com.tz.kumbukumbu

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.view.*
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tz.kumbukumbu.models.BoardSize
import com.tz.kumbukumbu.models.MemoryGame
import kumbukumbu.R
import android.content.Intent



class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "MainActivity"
    }

    private  lateinit var clRoot: ConstraintLayout
    private  lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView


    private lateinit  var memoryGame: MemoryGame
    private lateinit  var adapter: MemoryBoardAdapter

    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)

        setupBoard()
    }

    private fun setupBoard() {
        when (boardSize){
            BoardSize.EASY -> {
                tvMoves.text=getString(R.string.moves)
                tvPairs.text=getString(R.string.pair)
            }

            BoardSize.MEDIUM ->  {
                tvMoves.text=getString(R.string.normal)
                tvPairs.text=getString(R.string.pair_9)
            }
            BoardSize.HARD -> {
                tvMoves.text=getString(R.string.hard)
                tvPairs.text=getString(R.string.pair_12)
            }
            BoardSize.HARDEST -> {
                tvMoves.text=getString(R.string.very_hard)
                tvPairs.text=getString(R.string.pair_24)
            }
        }

        tvPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)
        adapter =  MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when(item.itemId){
           R.id.mi_refresh ->{
               if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                   showAlertDialog(getString(R.string.start_again),null, View.OnClickListener {
                       setupBoard()
                   })
               }else{
                   setupBoard()
               }
               return true
           }
           R.id.mi_new_size -> {
               showNewSizeDialog()
               return true
           }
           R.id.mi_share ->{
               val sharingIntent = Intent(Intent.ACTION_SEND)
               // type of the content to be shared
               sharingIntent.type = "text/plain"
               // Body of the content
               val shareBody = R.string.share_body
               // subject of the content. you can share anything
               val shareSubject = R.string.share_subject
               // passing body of the content
               sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
               // passing subject of the content
               sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)
               startActivity(Intent.createChooser(sharingIntent, R.string.share_using.toString()))
           }

       }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radio_group)
       when (boardSize){
           BoardSize.EASY -> radioGroupSize.check(R.id.rd_easy)
           BoardSize.MEDIUM ->radioGroupSize.check(R.id.rd_medium)
           BoardSize.HARD -> radioGroupSize.check(R.id.rd_hard)
           BoardSize.HARDEST -> radioGroupSize.check(R.id.rd_hardest)
       }

       showAlertDialog(getString(R.string.select_level), boardSizeView, View.OnClickListener {
        boardSize = when (radioGroupSize.checkedRadioButtonId){
            R.id.rd_easy -> BoardSize.EASY
            R.id.rd_medium ->  BoardSize.MEDIUM
            R.id.rd_hard ->  BoardSize.HARD
            else -> BoardSize.HARDEST
        }
           setupBoard()
       })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
       AlertDialog.Builder(this)
           .setTitle(title)
           .setView(view)
           .setNegativeButton(getString(R.string.accept), null)
           .setPositiveButton(getString(R.string.accept_sawa)){ _, _->
               positiveClickListener.onClick(null)
           }.show()
    }

    private fun updateGameWithFlip(position: Int) {
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot, getString(R.string.won), Snackbar.LENGTH_LONG).show()
            return
        }

        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, getString(R.string.not_ok), Snackbar.LENGTH_SHORT).show()
            return
        }
        if(memoryGame.flipCard(position)){
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int

            tvPairs.setTextColor(color)
            val pairStr = getString(R.string.pair22)
            tvPairs.text = String.format(pairStr , memoryGame.numPairsFound, boardSize.getNumPairs())
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, getString(R.string.messaje_pass), Snackbar.LENGTH_LONG).show()
            }
        }
        tvMoves.text = String.format(getString(R.string.moves22), memoryGame.getNumMoves())
        adapter.notifyDataSetChanged()

    }
}


