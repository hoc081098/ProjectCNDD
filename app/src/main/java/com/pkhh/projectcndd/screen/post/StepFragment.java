package com.pkhh.projectcndd.screen.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.pkhh.projectcndd.R;

import javax.annotation.Nonnull;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class StepFragment<T extends StepFragment.DataOutput> extends Fragment {
  protected T dataOutput;
  @Nullable private Unbinder unbinder = null;

  public T getDataOutput() {
    return dataOutput;
  }

  @Nonnull
  protected abstract T initialData();

  public boolean canGoNext() {
    if (isInvalidData()) {
      onInvalid();
      return false;
    }
    return true;
  }

  protected abstract boolean isInvalidData();

  protected void onInvalid() {
    if (getView() != null) {
      getView().startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shake_anim));
    }
  }

  @LayoutRes
  abstract int getLayoutId();

  @Nonnull
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(getLayoutId(), container, false);
  }

  @CallSuper
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);
    dataOutput = initialData();
  }

  @CallSuper
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  /**
   * Base class presents output data from [androidx.fragment.app.Fragment]
   */
  public interface DataOutput {}
}
