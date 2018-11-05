package com.pkhh.projectcndd.utils

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.pkhh.projectcndd.utils.BlurImageUtil.blurRenderScript
import com.squareup.picasso.Transformation

object BlurImageUtil {
  fun blurRenderScript(bitmap: Bitmap, radius: Float, context: Context): Bitmap {
    val applicationContext = context.applicationContext
    var rsContext: RenderScript? = null

    try {
      val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height,
        Bitmap.Config.ARGB_8888
      )

      // Create enqueueUpdateCurrentWeatherWorkRequestImmediately RenderScript context.
      rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.NORMAL)

      // Creates enqueueUpdateCurrentWeatherWorkRequestImmediately RenderScript allocation for the blurred result.
      val inAlloc = Allocation.createFromBitmap(rsContext, bitmap)
      val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)

      // Use the ScriptIntrinsicBlur intrinsic.
      ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
        .run {
          setRadius(radius)
          setInput(inAlloc)
          forEach(outAlloc)
        }

      // Copy to the output bitmap from the allocation.
      outAlloc.copyTo(output)
      return output
    } finally {
      rsContext?.finish()
      bitmap.recycle()
    }
  }
}

class BlurTransformation(
  context: Context,
  private val radius: Float = DEFAULT_RADIUS
) : Transformation {
  private val context = context.applicationContext

  override fun key() = toString()

  override fun transform(source: Bitmap): Bitmap = blurRenderScript(source, radius, context)

  override fun toString() = "BlurTransformation(radius=$radius)"

  companion object {
    const val DEFAULT_RADIUS = 25f
  }
}