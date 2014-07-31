package co.winsportsonline.wso.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Esteban- on 11-06-14.
 */
public class AnimationManager {

    public static  void press(final View v ){

        ObjectAnimator xUp = ObjectAnimator.ofFloat(v,"scaleX",1,1.2f);
        ObjectAnimator yUp = ObjectAnimator.ofFloat(v,"scaleY",1,1.2f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(v,"alpha",1,0.4f);
        final ObjectAnimator xDown = ObjectAnimator.ofFloat(v,"scaleX",1.2f,1);;
        final ObjectAnimator yDown = ObjectAnimator.ofFloat(v,"scaleY",1.2f,1);;

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(xUp,yUp,alpha);
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                AnimatorSet set = new AnimatorSet();
                set.playTogether(xDown, yDown);
                set.setDuration(500);
                set.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }
}
