package org.nuxeo.common.xmap;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

public class XDeferredAnnotatedMember extends XAnnotatedMember {

	public XDeferredAnnotatedMember(XAnnotatedMember original) {
		super(original.xmap, original.accessor);
		delegate = original;
	}

	protected final XAnnotatedMember delegate;

	protected static class ReifyedInvoke {
		protected final Context ctx;
		protected final Element element;
		ReifyedInvoke(Context ctx, Element element) {
			this.ctx = ctx;
			this.element = element;
		}
	}

	protected final List<ReifyedInvoke> deferredInvokes =
			new LinkedList<ReifyedInvoke>();

	@Override
	public void process(Context ctx, Element element) {
	    if (delegate.xmap.deferClassLoading) {
	        deferredInvokes.add(new ReifyedInvoke((Context)ctx.clone(), element));
	    } else {
	        delegate.process(ctx, element);
	    }
	}

	public void flush() {
		Iterator<ReifyedInvoke> it = deferredInvokes.iterator();
		while (it.hasNext()) {
			ReifyedInvoke invoke = it.next();
			it.remove();
			delegate.process(invoke.ctx, invoke.element);
		}
	}

}
