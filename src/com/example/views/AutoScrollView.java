package com.example.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class AutoScrollView extends HorizontalScrollView {
	private final Handler handler = new Handler();
	private long duration = 50;
	private boolean isScrolled = false;
	private int currentIndex = 0;
	private long period = 1000;
	private int currentX = 0;

	/**
	 * @param context
	 */
	public AutoScrollView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AutoScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AutoScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * �жϵ�ǰ�Ƿ�Ϊ����״̬
	 * 
	 * @return the isScrolled
	 */
	public boolean isScrolled() {
		return isScrolled;
	}

	/**
	 * �������߹ر��Զ���������
	 * 
	 * @param isScrolled
	 *            trueΪ������falseΪ�ر�
	 */
	public void setScrolled(boolean isScrolled) {
		this.isScrolled = isScrolled;
		autoScroll();
	}

	/**
	 * ��ȡ��ǰ��������βʱ��ͣ��ʱ�䣬��λ������
	 * 
	 * @return the period
	 */
	public long getPeriod() {
		return period;
	}

	/**
	 * ���õ�ǰ��������βʱ��ͣ��ʱ�䣬��λ������
	 * 
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(long period) {
		this.period = period;
	}

	/**
	 * ��ȡ��ǰ�Ĺ����ٶȣ���λ�����룬ֵԽС���ٶ�Խ�졣
	 * 
	 * @return the speed
	 */
	public long getSpeed() {
		return duration;
	}

	/**
	 * ���õ�ǰ�Ĺ����ٶȣ���λ�����룬ֵԽС���ٶ�Խ�졣
	 * 
	 * @param speed
	 *            the duration to set
	 */
	public void setSpeed(long speed) {
		this.duration = speed;
	}

	private void autoScroll() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isScrolled) {
					if (currentX == getScrollX() && currentX != 0) {
						try {
							Thread.sleep(period);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						currentIndex = 0;
						scrollTo(0, 0);
						handler.postDelayed(this, period);
					} else {
						currentX = getScrollX();
						handler.postDelayed(this, duration);
						currentIndex++;
						scrollTo(currentIndex * 5, 0);
					}
				}
			}
		}, duration);
	}

}
