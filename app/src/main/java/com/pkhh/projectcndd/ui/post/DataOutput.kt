package com.pkhh.projectcndd.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.android.gms.maps.model.LatLng
import com.pkhh.projectcndd.R

abstract class StepFragment<T : StepFragment.DataOutput> : Fragment() {
  protected lateinit var dataOutput: T
    private set

  abstract fun initialData(): T

  fun canGoNext(): Boolean {
    if (isInvalidData()) {
      onInvalid()
      return false
    }
    return true
  }

  abstract fun isInvalidData(): Boolean

  protected open fun onInvalid() {
    view?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shake_anim))
  }

  @LayoutRes
  abstract fun getLayoutId(): Int

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(getLayoutId(), container, false)

  private var unbinder: Unbinder? = null

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    unbinder = ButterKnife.bind(this, view)
    dataOutput = initialData()
  }

  @CallSuper
  override fun onDestroyView() {
    super.onDestroyView()
    unbinder?.unbind()
  }

  /**
   * Base class presents output data from [androidx.fragment.app.Fragment]
   */
  interface DataOutput
}

data class CategoryFragmentOutput @JvmOverloads constructor(var selectedCategoryId: String? = null) :
  StepFragment.DataOutput

data class AddressLocationFragmentOutput @JvmOverloads constructor(
  var provinceId: String? = null,
  var districtId: String? = null,
  var districtName: String? = null,
  var wardId: String? = null,
  var latLng: LatLng? = null,
  var address: String? = null
) : StepFragment.DataOutput

data class ImagesPhotosFragmentOutput @JvmOverloads constructor(
  val uris: List<Uri> = arrayListOf()
) : StepFragment.DataOutput

data class PriceTitleSizeDescriptionFragmentOutput @JvmOverloads constructor(
  var price: Long = 0,
  var title: String = "",
  var size: Double = 0.0,
  var description: String = "",
  var phone: String = ""
) : StepFragment.DataOutput