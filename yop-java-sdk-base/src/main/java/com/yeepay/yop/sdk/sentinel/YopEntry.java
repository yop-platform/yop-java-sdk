/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.ErrorEntryFreeException;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.function.BiConsumer;

import java.util.LinkedList;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/12/11
 */
public class YopEntry extends Entry {

    protected Entry parent = null;
    protected Entry child = null;

    protected ProcessorSlot<Object> chain;
    protected Context context;
    protected LinkedList<BiConsumer<Context, Entry>> exitHandlers;

    YopEntry(ResourceWrapper resourceWrapper, ProcessorSlot<Object> chain, Context context) {
        super(resourceWrapper);
        this.chain = chain;
        this.context = context;

        setUpEntryFor(context);
    }

    private void setUpEntryFor(Context context) {
        // The entry should not be associated to NullContext.
        if (context instanceof NullContext) {
            return;
        }
        this.parent = context.getCurEntry();
        if (parent != null) {
            ((YopEntry) parent).child = this;
        }
        context.setCurEntry(this);
    }

    @Override
    public void exit(int count, Object... args) throws ErrorEntryFreeException {
        trueExit(count, args);
    }

    /**
     * Note: the exit handlers will be called AFTER onExit of slot chain.
     */
    private void callExitHandlersAndCleanUp(Context ctx) {
        if (exitHandlers != null && !exitHandlers.isEmpty()) {
            for (BiConsumer<Context, Entry> handler : this.exitHandlers) {
                if (null == handler) {
                    continue;
                }
                try {
                    handler.accept(ctx, this);
                } catch (Exception e) {
                    RecordLog.warn("Error occurred when invoking entry exit handler, current entry: "
                            + resourceWrapper.getName(), e);
                }
            }
            exitHandlers = null;
        }
    }

    protected void exitForContext(Context context, int count, Object... args) throws ErrorEntryFreeException {
        if (context != null) {
            // Null context should exit without clean-up.
            if (context instanceof NullContext) {
                return;
            }

            if (context.getCurEntry() != this) {
                String curEntryNameInContext = context.getCurEntry() == null ? null
                        : context.getCurEntry().getResourceWrapper().getName();
                // Clean previous call stack.
                YopEntry e = (YopEntry) context.getCurEntry();
                while (e != null) {
                    e.exit(count, args);
                    e = (YopEntry) e.parent;
                }
                String errorMessage = String.format("The order of entry exit can't be paired with the order of entry"
                                + ", current entry in context: <%s>, but expected: <%s>", curEntryNameInContext,
                        resourceWrapper.getName());
                throw new ErrorEntryFreeException(errorMessage);
            } else {
                // Go through the onExit hook of all slots.
                if (chain != null) {
                    chain.exit(context, resourceWrapper, count, args);
                }
                // Go through the existing terminate handlers (associated to this invocation).
                callExitHandlersAndCleanUp(context);

                // Restore the call stack.
                context.setCurEntry(parent);
                if (parent != null) {
                    ((YopEntry) parent).child = null;
                }
                if (parent == null) {
                    // Default context (auto entered) will be exited automatically.
                    if (ContextUtil.isDefaultContext(context)) {
                        ContextUtil.exit();
                    }
                }
                // Clean the reference of context in current entry to avoid duplicate exit.
                clearEntryContext();
            }
        }
    }

    protected void clearEntryContext() {
        this.context = null;
    }

    @Override
    public void whenTerminate(BiConsumer<Context, Entry> handler) {
        if (this.exitHandlers == null) {
            this.exitHandlers = new LinkedList<>();
        }
        this.exitHandlers.add(handler);
    }

    @Override
    protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
        exitForContext(context, count, args);

        return parent;
    }

    @Override
    public Node getLastNode() {
        return parent == null ? null : parent.getCurNode();
    }
}
