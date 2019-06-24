package net.gunivers.gunibot.datas.serialize;

import java.util.ArrayList;
import java.util.Collection;

public class SerializerList extends ArrayList<Serializer>{

	private static final long serialVersionUID = 1L;

	public SerializerList() {
		super();
	}

	public SerializerList(SerializerList serializer_list) {
		super(serializer_list);
	}

	//	private void check() {
	//		HashSet<String> testSet = new HashSet<>();
	//
	//		for(Serializer serializer:this) {
	//			if(testSet.contains(serializer.systemId)) throw new SerializationIdConflictException("System id conflict ("+serializer.systemId+") within some serializer!", serializer);
	//			else testSet.add(serializer.systemId);
	//		}
	//	}

	private boolean willConflict(Serializer serializer) {
		return willConflict(this, serializer);
	}

	private static boolean willConflict(SerializerList list, Serializer serializer) {
		for(Serializer int_serializer:list) {
			if(serializer.systemId.equals(int_serializer.systemId)) return false;
		}
		return true;
	}

	@Override
	public Serializer set(int index, Serializer element) {
		SerializerList simulation = new SerializerList(this);
		simulation.remove(index);
		if(willConflict(simulation, element)) throw new SerializationIdConflictException("System id conflict ("+element.systemId+") within some serializer!", element);
		else return super.set(index, element);
	}

	@Override
	public boolean add(Serializer e) {
		if(willConflict(e)) throw new SerializationIdConflictException("System id conflict ("+e.systemId+") within some serializer!", e);
		else return super.add(e);
	}

	@Override
	public void add(int index, Serializer element) {
		if(willConflict(element)) throw new SerializationIdConflictException("System id conflict ("+element.systemId+") within some serializer!", element);
		else super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends Serializer> c) {
		for(Serializer element:c) {
			if(willConflict(element)) throw new SerializationIdConflictException("System id conflict ("+element.systemId+") within some serializer!", element);
		}
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Serializer> c) {
		for(Serializer element:c) {
			if(willConflict(element)) throw new SerializationIdConflictException("System id conflict ("+element.systemId+") within some serializer!", element);
		}
		return super.addAll(index, c);
	}

}
