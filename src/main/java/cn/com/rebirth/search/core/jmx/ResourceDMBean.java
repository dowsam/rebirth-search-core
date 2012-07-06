/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ResourceDMBean.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.ServiceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.search.commons.Classes;
import cn.com.rebirth.search.commons.Preconditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


/**
 * The Class ResourceDMBean.
 *
 * @author l.xue.nong
 */
public class ResourceDMBean implements DynamicMBean {

	
	/** The Constant primitives. */
	private static final Class<?>[] primitives = { int.class, byte.class, short.class, long.class, float.class,
			double.class, boolean.class, char.class };

	
	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	/** The obj. */
	private final Object obj;

	
	/** The object name. */
	private final String objectName;

	
	/** The group name. */
	private final String groupName;

	
	/** The full object name. */
	private final String fullObjectName;

	
	/** The description. */
	private final String description;

	
	/** The attributes info. */
	private final MBeanAttributeInfo[] attributesInfo;

	
	/** The operations info. */
	private final MBeanOperationInfo[] operationsInfo;

	
	/** The m bean info. */
	private final MBeanInfo mBeanInfo;

	
	/** The attributes. */
	private final ImmutableMap<String, AttributeEntry> attributes;

	
	/** The operations. */
	private final ImmutableList<MBeanOperationInfo> operations;

	
	/**
	 * Instantiates a new resource dm bean.
	 *
	 * @param instance the instance
	 */
	public ResourceDMBean(Object instance) {
		Preconditions.checkNotNull(instance, "Cannot make an MBean wrapper for null instance");
		this.obj = instance;

		MapBuilder<String, AttributeEntry> attributesBuilder = MapBuilder.newMapBuilder();
		List<MBeanOperationInfo> operationsBuilder = new ArrayList<MBeanOperationInfo>();

		MBean mBean = obj.getClass().getAnnotation(MBean.class);

		this.groupName = findGroupName();

		if (mBean != null && Strings.hasLength(mBean.objectName())) {
			objectName = mBean.objectName();
		} else {
			if (Strings.hasLength(groupName)) {
				
				objectName = "";
			} else {
				objectName = obj.getClass().getSimpleName();
			}
		}

		StringBuilder sb = new StringBuilder(groupName);
		if (Strings.hasLength(groupName) && Strings.hasLength(objectName)) {
			sb.append(",");
		}
		sb.append(objectName);
		this.fullObjectName = sb.toString();

		this.description = findDescription();
		findFields(attributesBuilder);
		findMethods(attributesBuilder, operationsBuilder);

		this.attributes = attributesBuilder.immutableMap();
		this.operations = ImmutableList.copyOf(operationsBuilder);

		attributesInfo = new MBeanAttributeInfo[attributes.size()];
		int i = 0;

		MBeanAttributeInfo info;
		for (AttributeEntry entry : attributes.values()) {
			info = entry.getInfo();
			attributesInfo[i++] = info;
			if (logger.isInfoEnabled()) {
				logger.trace("Attribute " + info.getName() + "[r=" + info.isReadable() + ",w=" + info.isWritable()
						+ ",is=" + info.isIs() + ",type=" + info.getType() + "]");
			}
		}

		operationsInfo = new MBeanOperationInfo[operations.size()];
		operations.toArray(operationsInfo);

		if (logger.isTraceEnabled()) {
			if (operations.size() > 0)
				logger.trace("Operations are:");
			for (MBeanOperationInfo op : operationsInfo) {
				logger.trace("Operation " + op.getReturnType() + " " + op.getName());
			}
		}

		this.mBeanInfo = new MBeanInfo(getObject().getClass().getCanonicalName(), description, attributesInfo, null,
				operationsInfo, null);
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {
		return mBeanInfo;
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
		if (name == null || name.length() == 0)
			throw new NullPointerException("Invalid attribute requested " + name);

		Attribute attr = getNamedAttribute(name);
		if (attr == null) {
			throw new AttributeNotFoundException("Unknown attribute '" + name + "'. Known attributes names are: "
					+ attributes.keySet());
		}
		return attr.getValue();
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public synchronized void setAttribute(Attribute attribute) {
		if (attribute == null || attribute.getName() == null)
			throw new NullPointerException("Invalid attribute requested " + attribute);

		setNamedAttribute(attribute);
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public synchronized AttributeList getAttributes(String[] names) {
		AttributeList al = new AttributeList();
		for (String name : names) {
			Attribute attr = getNamedAttribute(name);
			if (attr != null) {
				al.add(attr);
			} else {
				logger.warn("Did not find attribute " + name);
			}
		}
		return al;
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public synchronized AttributeList setAttributes(AttributeList list) {
		AttributeList results = new AttributeList();
		for (Object aList : list) {
			Attribute attr = (Attribute) aList;

			if (setNamedAttribute(attr)) {
				results.add(attr);
			} else {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to update attribute name " + attr.getName() + " with value " + attr.getValue());
				}
			}
		}
		return results;
	}

	
	/* (non-Javadoc)
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String name, Object[] args, String[] sig) throws MBeanException, ReflectionException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoke method called on " + name);
		}

		MBeanOperationInfo opInfo = null;
		for (MBeanOperationInfo op : operationsInfo) {
			if (op.getName().equals(name)) {
				opInfo = op;
				break;
			}
		}

		if (opInfo == null) {
			final String msg = "Operation " + name + " not in ModelMBeanInfo";
			throw new MBeanException(new ServiceNotFoundException(msg), msg);
		}

		try {
			Class<?>[] classes = new Class[sig.length];
			for (int i = 0; i < classes.length; i++) {
				classes[i] = getClassForName(sig[i]);
			}
			Method method = getObject().getClass().getMethod(name, classes);
			return method.invoke(getObject(), args);
		} catch (Exception e) {
			throw new MBeanException(e);
		}
	}

	
	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	Object getObject() {
		return obj;
	}

	
	/**
	 * Gets the class for name.
	 *
	 * @param name the name
	 * @return the class for name
	 * @throws ClassNotFoundException the class not found exception
	 */
	private Class<?> getClassForName(String name) throws ClassNotFoundException {
		try {
			return Classes.getDefaultClassLoader().loadClass(name);
		} catch (ClassNotFoundException cnfe) {
			
			for (Class<?> primitive : primitives) {
				if (name.equals(primitive.getName())) {
					return primitive;
				}
			}
		}
		throw new ClassNotFoundException("Class " + name + " cannot be found");
	}

	
	/**
	 * Find group name.
	 *
	 * @return the string
	 */
	private String findGroupName() {
		Class objClass = getObject().getClass();
		while (objClass != Object.class) {
			Method[] methods = objClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(ManagedGroupName.class)) {
					try {
						method.setAccessible(true);
						return (String) method.invoke(getObject());
					} catch (Exception e) {
						logger.warn("Failed to get group name for [" + getObject() + "]", e);
					}
				}
			}
			objClass = objClass.getSuperclass();
		}
		return "";
	}

	
	/**
	 * Find description.
	 *
	 * @return the string
	 */
	private String findDescription() {
		MBean mbean = getObject().getClass().getAnnotation(MBean.class);
		if (mbean != null && mbean.description() != null && mbean.description().trim().length() > 0) {
			return mbean.description();
		}
		return "";
	}

	
	/**
	 * Find methods.
	 *
	 * @param attributesBuilder the attributes builder
	 * @param ops the ops
	 */
	private void findMethods(MapBuilder<String, AttributeEntry> attributesBuilder, List<MBeanOperationInfo> ops) {
		
		List<Method> methods = new ArrayList<Method>(Arrays.asList(getObject().getClass().getMethods()));
		List<Method> objectMethods = new ArrayList<Method>(Arrays.asList(Object.class.getMethods()));
		methods.removeAll(objectMethods);

		for (Method method : methods) {
			
			ManagedAttribute attr = method.getAnnotation(ManagedAttribute.class);
			if (attr != null) {
				String methodName = method.getName();
				if (!methodName.startsWith("get") && !methodName.startsWith("set") && !methodName.startsWith("is")) {
					if (logger.isWarnEnabled())
						logger.warn("method name " + methodName + " doesn't start with \"get\", \"set\", or \"is\""
								+ ", but is annotated with @ManagedAttribute: will be ignored");
				} else {
					MBeanAttributeInfo info;
					String attributeName = null;
					boolean writeAttribute = false;
					if (isSetMethod(method)) { 
						attributeName = methodName.substring(3);
						info = new MBeanAttributeInfo(attributeName, method.getParameterTypes()[0].getCanonicalName(),
								attr.description(), true, true, false);
						writeAttribute = true;
					} else { 
						if (method.getParameterTypes().length == 0 && method.getReturnType() != java.lang.Void.TYPE) {
							boolean hasSetter = attributesBuilder.containsKey(attributeName);
							
							if (methodName.startsWith("is")) {
								attributeName = methodName.substring(2);
								info = new MBeanAttributeInfo(attributeName, method.getReturnType().getCanonicalName(),
										attr.description(), true, hasSetter, true);
							} else {
								
								attributeName = methodName.substring(3);
								info = new MBeanAttributeInfo(attributeName, method.getReturnType().getCanonicalName(),
										attr.description(), true, hasSetter, false);
							}
						} else {
							if (logger.isWarnEnabled()) {
								logger.warn("Method " + method.getName()
										+ " must have a valid return type and zero parameters");
							}
							continue;
						}
					}

					AttributeEntry ae = attributesBuilder.get(attributeName);
					
					if (!writeAttribute) {
						
						if (ae instanceof FieldAttributeEntry && ae.getInfo().isReadable()) {
							logger.warn("not adding annotated method " + method
									+ " since we already have read attribute");
						}
						
						else if (ae instanceof MethodAttributeEntry) {
							MethodAttributeEntry mae = (MethodAttributeEntry) ae;
							if (mae.hasSetMethod()) {
								attributesBuilder.put(attributeName,
										new MethodAttributeEntry(mae.getInfo(), mae.getSetMethod(), method));
							}
						} 
						else {
							attributesBuilder.put(attributeName, new MethodAttributeEntry(info, null, method));
						}
					}
					else {
						if (ae instanceof FieldAttributeEntry) {
							
							if (ae.getInfo().isWritable()) {
								logger.warn("Not adding annotated method " + methodName
										+ " since we already have writable attribute");
							} else {
								
								
								Field f = ((FieldAttributeEntry) ae).getField();
								MBeanAttributeInfo i = new MBeanAttributeInfo(ae.getInfo().getName(), f.getType()
										.getCanonicalName(), attr.description(), true, !Modifier.isFinal(f
										.getModifiers()), false);
								attributesBuilder.put(attributeName, new FieldAttributeEntry(i, f));
							}
						}
						
						else if (ae instanceof MethodAttributeEntry) {
							MethodAttributeEntry mae = (MethodAttributeEntry) ae;
							if (mae.hasIsOrGetMethod()) {
								attributesBuilder.put(attributeName,
										new MethodAttributeEntry(info, method, mae.getIsOrGetMethod()));
							}
						} 
						else {
							attributesBuilder.put(attributeName, new MethodAttributeEntry(info, method, null));
						}
					}
				}
			} else if (method.isAnnotationPresent(ManagedOperation.class)) {
				ManagedOperation op = method.getAnnotation(ManagedOperation.class);
				String attName = method.getName();
				if (isSetMethod(method) || isGetMethod(method)) {
					attName = attName.substring(3);
				} else if (isIsMethod(method)) {
					attName = attName.substring(2);
				}
				
				boolean isAlreadyExposed = attributesBuilder.containsKey(attName);
				if (!isAlreadyExposed) {
					ops.add(new MBeanOperationInfo(op != null ? op.description() : "", method));
				}
			}
		}
	}

	
	/**
	 * Checks if is sets the method.
	 *
	 * @param method the method
	 * @return true, if is sets the method
	 */
	private boolean isSetMethod(Method method) {
		return (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && method.getReturnType() == java.lang.Void.TYPE);
	}

	
	/**
	 * Checks if is gets the method.
	 *
	 * @param method the method
	 * @return true, if is gets the method
	 */
	private boolean isGetMethod(Method method) {
		return (method.getParameterTypes().length == 0 && method.getReturnType() != java.lang.Void.TYPE && method
				.getName().startsWith("get"));
	}

	
	/**
	 * Checks if is checks if is method.
	 *
	 * @param method the method
	 * @return true, if is checks if is method
	 */
	private boolean isIsMethod(Method method) {
		return (method.getParameterTypes().length == 0
				&& (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) && method
				.getName().startsWith("is"));
	}

	
	/**
	 * Find fields.
	 *
	 * @param attributesBuilder the attributes builder
	 */
	private void findFields(MapBuilder<String, AttributeEntry> attributesBuilder) {
		
		for (Class<?> clazz = getObject().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				ManagedAttribute attr = field.getAnnotation(ManagedAttribute.class);
				if (attr != null) {
					String fieldName = renameToJavaCodingConvention(field.getName());
					MBeanAttributeInfo info = new MBeanAttributeInfo(fieldName, field.getType().getCanonicalName(),
							attr.description(), true, !Modifier.isFinal(field.getModifiers()) && attr.writable(), false);
					attributesBuilder.put(fieldName, new FieldAttributeEntry(info, field));
				}
			}
		}
	}

	
	/**
	 * Gets the named attribute.
	 *
	 * @param name the name
	 * @return the named attribute
	 */
	private Attribute getNamedAttribute(String name) {
		Attribute result = null;
		AttributeEntry entry = attributes.get(name);
		if (entry != null) {
			MBeanAttributeInfo i = entry.getInfo();
			try {
				result = new Attribute(name, entry.invoke(null));
				if (logger.isDebugEnabled())
					logger.debug("Attribute " + name + " has r=" + i.isReadable() + ",w=" + i.isWritable() + ",is="
							+ i.isIs() + " and value " + result.getValue());
			} catch (Exception e) {
				logger.debug("Exception while reading value of attribute " + name, e);
			}
		} else {
			logger.warn("Did not find queried attribute with name " + name);
		}
		return result;
	}

	
	/**
	 * Sets the named attribute.
	 *
	 * @param attribute the attribute
	 * @return true, if successful
	 */
	private boolean setNamedAttribute(Attribute attribute) {
		boolean result = false;
		if (logger.isDebugEnabled())
			logger.debug("Invoking set on attribute " + attribute.getName() + " with value " + attribute.getValue());

		AttributeEntry entry = attributes.get(attribute.getName());
		if (entry != null) {
			try {
				entry.invoke(attribute);
				result = true;
			} catch (Exception e) {
				logger.warn("Exception while writing value for attribute " + attribute.getName(), e);
			}
		} else {
			logger.warn("Could not invoke set on attribute " + attribute.getName() + " with value "
					+ attribute.getValue());
		}
		return result;
	}

	
	/**
	 * Rename to java coding convention.
	 *
	 * @param fieldName the field name
	 * @return the string
	 */
	private String renameToJavaCodingConvention(String fieldName) {
		if (fieldName.contains("_")) {
			Pattern p = Pattern.compile("_.");
			Matcher m = p.matcher(fieldName);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(sb, fieldName.substring(m.end() - 1, m.end()).toUpperCase());
			}
			m.appendTail(sb);
			char first = sb.charAt(0);
			if (Character.isLowerCase(first)) {
				sb.setCharAt(0, Character.toUpperCase(first));
			}
			return sb.toString();
		} else {
			if (Character.isLowerCase(fieldName.charAt(0))) {
				return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			} else {
				return fieldName;
			}
		}
	}

	
	/**
	 * The Class MethodAttributeEntry.
	 *
	 * @author l.xue.nong
	 */
	private class MethodAttributeEntry implements AttributeEntry {

		
		/** The info. */
		final MBeanAttributeInfo info;

		
		/** The is or getmethod. */
		final Method isOrGetmethod;

		
		/** The set method. */
		final Method setMethod;

		
		/**
		 * Instantiates a new method attribute entry.
		 *
		 * @param info the info
		 * @param setMethod the set method
		 * @param isOrGetMethod the is or get method
		 */
		public MethodAttributeEntry(final MBeanAttributeInfo info, final Method setMethod, final Method isOrGetMethod) {
			super();
			this.info = info;
			this.setMethod = setMethod;
			this.isOrGetmethod = isOrGetMethod;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.jmx.ResourceDMBean.AttributeEntry#invoke(javax.management.Attribute)
		 */
		public Object invoke(Attribute a) throws Exception {
			if (a == null && isOrGetmethod != null)
				return isOrGetmethod.invoke(getObject());
			else if (a != null && setMethod != null)
				return setMethod.invoke(getObject(), a.getValue());
			else
				return null;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.jmx.ResourceDMBean.AttributeEntry#getInfo()
		 */
		public MBeanAttributeInfo getInfo() {
			return info;
		}

		
		/**
		 * Checks for is or get method.
		 *
		 * @return true, if successful
		 */
		public boolean hasIsOrGetMethod() {
			return isOrGetmethod != null;
		}

		
		/**
		 * Checks for set method.
		 *
		 * @return true, if successful
		 */
		public boolean hasSetMethod() {
			return setMethod != null;
		}

		
		/**
		 * Gets the checks if is or get method.
		 *
		 * @return the checks if is or get method
		 */
		public Method getIsOrGetMethod() {
			return isOrGetmethod;
		}

		
		/**
		 * Gets the sets the method.
		 *
		 * @return the sets the method
		 */
		public Method getSetMethod() {
			return setMethod;
		}
	}

	
	/**
	 * The Class FieldAttributeEntry.
	 *
	 * @author l.xue.nong
	 */
	private class FieldAttributeEntry implements AttributeEntry {

		
		/** The info. */
		private final MBeanAttributeInfo info;

		
		/** The field. */
		private final Field field;

		
		/**
		 * Instantiates a new field attribute entry.
		 *
		 * @param info the info
		 * @param field the field
		 */
		public FieldAttributeEntry(final MBeanAttributeInfo info, final Field field) {
			super();
			this.info = info;
			this.field = field;
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
		}

		
		/**
		 * Gets the field.
		 *
		 * @return the field
		 */
		public Field getField() {
			return field;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.jmx.ResourceDMBean.AttributeEntry#invoke(javax.management.Attribute)
		 */
		public Object invoke(Attribute a) throws Exception {
			if (a == null) {
				return field.get(getObject());
			} else {
				field.set(getObject(), a.getValue());
				return null;
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.jmx.ResourceDMBean.AttributeEntry#getInfo()
		 */
		public MBeanAttributeInfo getInfo() {
			return info;
		}
	}

	
	/**
	 * The Interface AttributeEntry.
	 *
	 * @author l.xue.nong
	 */
	private interface AttributeEntry {

		
		/**
		 * Invoke.
		 *
		 * @param a the a
		 * @return the object
		 * @throws Exception the exception
		 */
		public Object invoke(Attribute a) throws Exception;

		
		/**
		 * Gets the info.
		 *
		 * @return the info
		 */
		public MBeanAttributeInfo getInfo();
	}

	
	/**
	 * Checks if is managed resource.
	 *
	 * @return true, if is managed resource
	 */
	public boolean isManagedResource() {
		return !attributes.isEmpty() || !operations.isEmpty();
	}

	
	/**
	 * Gets the full object name.
	 *
	 * @return the full object name
	 */
	public String getFullObjectName() {
		return this.fullObjectName;
	}

	
	/**
	 * Gets the object name.
	 *
	 * @return the object name
	 */
	public String getObjectName() {
		return this.objectName;
	}

	
	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return this.groupName;
	}
}