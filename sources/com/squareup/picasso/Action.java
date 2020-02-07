package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso.Priority;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

abstract class Action<T> {
    boolean cancelled;
    final Drawable errorDrawable;
    final int errorResId;
    final String key;
    final int memoryPolicy;
    final int networkPolicy;
    final boolean noFade;
    final Picasso picasso;
    final Request request;
    final Object tag;
    final WeakReference<T> target;
    boolean willReplay;

    static class RequestWeakReference<M> extends WeakReference<M> {
        final Action action;

        public RequestWeakReference(Action action2, M referent, ReferenceQueue<? super M> q) {
            super(referent, q);
            this.action = action2;
        }
    }

    /* access modifiers changed from: 0000 */
    public abstract void complete(Bitmap bitmap, LoadedFrom loadedFrom);

    /* access modifiers changed from: 0000 */
    public abstract void error();

    Action(Picasso picasso2, T target2, Request request2, int memoryPolicy2, int networkPolicy2, int errorResId2, Drawable errorDrawable2, String key2, Object tag2, boolean noFade2) {
        this.picasso = picasso2;
        this.request = request2;
        this.target = target2 == null ? null : new RequestWeakReference(this, target2, picasso2.referenceQueue);
        this.memoryPolicy = memoryPolicy2;
        this.networkPolicy = networkPolicy2;
        this.noFade = noFade2;
        this.errorResId = errorResId2;
        this.errorDrawable = errorDrawable2;
        this.key = key2;
        if (tag2 == 0) {
            tag2 = this;
        }
        this.tag = tag2;
    }

    /* access modifiers changed from: 0000 */
    public void cancel() {
        this.cancelled = true;
    }

    /* access modifiers changed from: 0000 */
    public Request getRequest() {
        return this.request;
    }

    /* access modifiers changed from: 0000 */
    public T getTarget() {
        if (this.target == null) {
            return null;
        }
        return this.target.get();
    }

    /* access modifiers changed from: 0000 */
    public String getKey() {
        return this.key;
    }

    /* access modifiers changed from: 0000 */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /* access modifiers changed from: 0000 */
    public boolean willReplay() {
        return this.willReplay;
    }

    /* access modifiers changed from: 0000 */
    public int getMemoryPolicy() {
        return this.memoryPolicy;
    }

    /* access modifiers changed from: 0000 */
    public int getNetworkPolicy() {
        return this.networkPolicy;
    }

    /* access modifiers changed from: 0000 */
    public Picasso getPicasso() {
        return this.picasso;
    }

    /* access modifiers changed from: 0000 */
    public Priority getPriority() {
        return this.request.priority;
    }

    /* access modifiers changed from: 0000 */
    public Object getTag() {
        return this.tag;
    }
}
