/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MembershipAction.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen.membership;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.FutureTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;


/**
 * The Class MembershipAction.
 *
 * @author l.xue.nong
 */
public class MembershipAction extends AbstractComponent {

	
	/**
	 * The listener interface for receiving membership events.
	 * The class that is interested in processing a membership
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMembershipListener<code> method. When
	 * the membership event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MembershipEvent
	 */
	public static interface MembershipListener {

		
		/**
		 * On join.
		 *
		 * @param node the node
		 * @return the cluster state
		 */
		ClusterState onJoin(DiscoveryNode node);

		
		/**
		 * On leave.
		 *
		 * @param node the node
		 */
		void onLeave(DiscoveryNode node);
	}

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The nodes provider. */
	private final DiscoveryNodesProvider nodesProvider;

	
	/** The listener. */
	private final MembershipListener listener;

	
	/**
	 * Instantiates a new membership action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param nodesProvider the nodes provider
	 * @param listener the listener
	 */
	public MembershipAction(Settings settings, TransportService transportService, DiscoveryNodesProvider nodesProvider,
			MembershipListener listener) {
		super(settings);
		this.transportService = transportService;
		this.nodesProvider = nodesProvider;
		this.listener = listener;

		transportService.registerHandler(JoinRequestRequestHandler.ACTION, new JoinRequestRequestHandler());
		transportService.registerHandler(LeaveRequestRequestHandler.ACTION, new LeaveRequestRequestHandler());
	}

	
	/**
	 * Close.
	 */
	public void close() {
		transportService.removeHandler(JoinRequestRequestHandler.ACTION);
		transportService.removeHandler(LeaveRequestRequestHandler.ACTION);
	}

	
	/**
	 * Send leave request.
	 *
	 * @param masterNode the master node
	 * @param node the node
	 */
	public void sendLeaveRequest(DiscoveryNode masterNode, DiscoveryNode node) {
		transportService.sendRequest(node, LeaveRequestRequestHandler.ACTION, new LeaveRequest(masterNode),
				VoidTransportResponseHandler.INSTANCE_SAME);
	}

	
	/**
	 * Send leave request blocking.
	 *
	 * @param masterNode the master node
	 * @param node the node
	 * @param timeout the timeout
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public void sendLeaveRequestBlocking(DiscoveryNode masterNode, DiscoveryNode node, TimeValue timeout)
			throws RestartException {
		transportService.submitRequest(masterNode, LeaveRequestRequestHandler.ACTION, new LeaveRequest(node),
				VoidTransportResponseHandler.INSTANCE_SAME).txGet(timeout.millis(), TimeUnit.MILLISECONDS);
	}

	
	/**
	 * Send join request.
	 *
	 * @param masterNode the master node
	 * @param node the node
	 */
	public void sendJoinRequest(DiscoveryNode masterNode, DiscoveryNode node) {
		transportService.sendRequest(masterNode, JoinRequestRequestHandler.ACTION, new JoinRequest(node, false),
				VoidTransportResponseHandler.INSTANCE_SAME);
	}

	
	/**
	 * Send join request blocking.
	 *
	 * @param masterNode the master node
	 * @param node the node
	 * @param timeout the timeout
	 * @return the cluster state
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public ClusterState sendJoinRequestBlocking(DiscoveryNode masterNode, DiscoveryNode node, TimeValue timeout)
			throws RestartException {
		return transportService.submitRequest(masterNode, JoinRequestRequestHandler.ACTION,
				new JoinRequest(node, true), new FutureTransportResponseHandler<JoinResponse>() {
					@Override
					public JoinResponse newInstance() {
						return new JoinResponse();
					}
				}).txGet(timeout.millis(), TimeUnit.MILLISECONDS).clusterState;
	}

	
	/**
	 * The Class JoinRequest.
	 *
	 * @author l.xue.nong
	 */
	static class JoinRequest implements Streamable {

		
		/** The node. */
		DiscoveryNode node;

		
		/** The with cluster state. */
		boolean withClusterState;

		
		/**
		 * Instantiates a new join request.
		 */
		private JoinRequest() {
		}

		
		/**
		 * Instantiates a new join request.
		 *
		 * @param node the node
		 * @param withClusterState the with cluster state
		 */
		private JoinRequest(DiscoveryNode node, boolean withClusterState) {
			this.node = node;
			this.withClusterState = withClusterState;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			node = DiscoveryNode.readNode(in);
			withClusterState = in.readBoolean();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			node.writeTo(out);
			out.writeBoolean(withClusterState);
		}
	}

	
	/**
	 * The Class JoinResponse.
	 *
	 * @author l.xue.nong
	 */
	class JoinResponse implements Streamable {

		
		/** The cluster state. */
		ClusterState clusterState;

		
		/**
		 * Instantiates a new join response.
		 */
		JoinResponse() {
		}

		
		/**
		 * Instantiates a new join response.
		 *
		 * @param clusterState the cluster state
		 */
		JoinResponse(ClusterState clusterState) {
			this.clusterState = clusterState;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			clusterState = ClusterState.Builder.readFrom(in, nodesProvider.nodes().localNode());
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			ClusterState.Builder.writeTo(clusterState, out);
		}
	}

	
	/**
	 * The Class JoinRequestRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class JoinRequestRequestHandler extends BaseTransportRequestHandler<JoinRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "discovery/zen/join";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public JoinRequest newInstance() {
			return new JoinRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(JoinRequest request, TransportChannel channel) throws Exception {
			ClusterState clusterState = listener.onJoin(request.node);
			if (request.withClusterState) {
				channel.sendResponse(new JoinResponse(clusterState));
			} else {
				channel.sendResponse(VoidStreamable.INSTANCE);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}
	}

	
	/**
	 * The Class LeaveRequest.
	 *
	 * @author l.xue.nong
	 */
	private static class LeaveRequest implements Streamable {

		
		/** The node. */
		private DiscoveryNode node;

		
		/**
		 * Instantiates a new leave request.
		 */
		private LeaveRequest() {
		}

		
		/**
		 * Instantiates a new leave request.
		 *
		 * @param node the node
		 */
		private LeaveRequest(DiscoveryNode node) {
			this.node = node;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			node = DiscoveryNode.readNode(in);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			node.writeTo(out);
		}
	}

	
	/**
	 * The Class LeaveRequestRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class LeaveRequestRequestHandler extends BaseTransportRequestHandler<LeaveRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "discovery/zen/leave";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public LeaveRequest newInstance() {
			return new LeaveRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(LeaveRequest request, TransportChannel channel) throws Exception {
			listener.onLeave(request.node);
			channel.sendResponse(VoidStreamable.INSTANCE);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}
}
