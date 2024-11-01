package se.umu.ad.anpa0292.voxelsculpter

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var editorSurfaceView: EditorSurfaceView
    private lateinit var addButton: ImageButton
    private lateinit var removeButton: ImageButton
    private lateinit var paintButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editorSurfaceView = findViewById(R.id.editor_surface_view)
        addButton = findViewById(R.id.add_button)
        removeButton = findViewById(R.id.remove_button)
        paintButton = findViewById(R.id.paint_button)

        addButton.setOnClickListener {
            editorSurfaceView.currentTool = Tool.ADD
        }

        removeButton.setOnClickListener {
            editorSurfaceView.currentTool = Tool.REMOVE
        }

        paintButton.setOnClickListener {
            editorSurfaceView.currentTool = Tool.PAINT
        }
    }

    override fun onPause() {
        super.onPause()
        editorSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        editorSurfaceView.onResume()
    }
}