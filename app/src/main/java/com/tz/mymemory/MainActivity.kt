package com.tz.mymemory

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
import com.tz.mymemory.models.BoardSize
import com.tz.mymemory.models.MemoryGame

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
                tvMoves.text="Rahisi: 4 x 2"
                tvPairs.text="Jozi: 0 / 4"
            }

            BoardSize.MEDIUM ->  {
                tvMoves.text="Kawaida: 6 x 3"
                tvPairs.text="Jozi: 0 / 9"
            }
            BoardSize.HARD -> {
                tvMoves.text="Ngumu: 6 x 6"
                tvPairs.text="Jozi: 0 / 12"
            }
            BoardSize.HARDEST -> {
                tvMoves.text="Ngumu Sana: 6 x 8"
                tvPairs.text="Jozi: 0 / 24"
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
                   showAlertDialog("Unaanza upya?",null, View.OnClickListener {
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

       showAlertDialog("Chagua kiwango", boardSizeView, View.OnClickListener {
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
           .setNegativeButton("Hapana", null)
           .setPositiveButton("Sawa"){_,_->
               positiveClickListener.onClick(null)
           }.show()
    }

    private fun updateGameWithFlip(position: Int) {
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot, "Ulishinda!", Snackbar.LENGTH_LONG).show()
            return
        }

        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, "SI sahihi!", Snackbar.LENGTH_SHORT).show()
            return
        }
        if(memoryGame.flipCard(position)){
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int

            tvPairs.setTextColor(color)
            tvPairs.text = "Jozi: ${memoryGame.numPairsFound}/${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, "Umeshinda! Hongera", Snackbar.LENGTH_LONG).show()
            }
        }
        tvMoves.text = "Hatua: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()

    }
}

private fun TextView.setTextColor(color: Any) {

}
