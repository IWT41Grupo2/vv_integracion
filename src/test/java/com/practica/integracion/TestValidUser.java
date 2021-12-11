package com.practica.integracion;

import com.practica.integracion.DAO.AuthDAO;
import com.practica.integracion.DAO.GenericDAO;
import com.practica.integracion.DAO.User;
import com.practica.integracion.manager.SystemManager;
import com.practica.integracion.manager.SystemManagerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TestValidUser {

	@Mock
	private static AuthDAO mockAuthDao;
	@Mock
	private static GenericDAO mockGenericDao;

	@Test
	public void testStartRemoteSystemWithValidUserAndSystem() throws Exception {

		User validUser = new User("1","Ana","Lopez","Madrid", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario valido
		when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);

		String validId = "12345"; // id valido de sistema
		ArrayList<Object> lista = new ArrayList<>(Arrays.asList("uno", "dos"));
		when(mockGenericDao.getSomeData(validUser, "where id=" + validId)).thenReturn(lista);
		// primero debe ejecutarse la llamada al dao de autenticación
		// despues la de  acceso a datos del sistema (las validaciones del orden en cada prueba)
		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);
		// instanciamos el manager con los mock creados
		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);
		// llamada al api a probar
		Collection<Object> retorno = manager.startRemoteSystem(validUser.getId(), validId);
		assertEquals(retorno.toString(), "[uno, dos]");
		// vemos si se ejecutan las llamadas a los dao, y en el orden correcto
		ordered.verify(mockAuthDao).getAuthData(validUser.getId());
		ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + validId);
	}

	@Test
	public void testStartRemoteSystemWithValidUserAndInvalidSystem() throws Exception {

		User validUser = new User("1","Ana","Lopez","Madrid", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario valido
		when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);

		String invalidId = "10000"; // id NO valido de sistema
		when(mockGenericDao.getSomeData(validUser, "where id=" + invalidId)).thenThrow(new OperationNotSupportedException());

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		//en este caso no tiene que devolver nada, sino tirar una excepción
		assertThrows(SystemManagerException.class, () -> { manager.startRemoteSystem(validUser.getId(), invalidId);});

		ordered.verify(mockAuthDao).getAuthData(validUser.getId());
		ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + invalidId);
	}

	@Test
	public void testStartRemoteSystemWithValidUserAndNullSystem() throws Exception {

		User validUser = new User("1","Ana","Lopez","Madrid", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario valido
		when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);

		String nullId = null; // id nulo de sistema
		ArrayList<Object> lista1 = new ArrayList<>(Arrays.asList("uno", "dos"));
		ArrayList<Object> lista2 = new ArrayList<>(Arrays.asList("1", "2"));
		when(mockGenericDao.getSomeData(validUser, "where id=" + nullId)).thenReturn(Arrays.asList(lista1,lista2));

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		Collection<Object> retorno = manager.startRemoteSystem(validUser.getId(), nullId);
		assertEquals(retorno.toString(), "[[uno, dos], [1, 2]]");

		ordered.verify(mockAuthDao).getAuthData(validUser.getId());
		ordered.verify(mockGenericDao).getSomeData(validUser, "where id=" + nullId);
	}

	//stopRemoteSystem tiene el mismo código que startRemoteSystem,
	// por lo que los test de startRemoteSystem también prueban stopRemoteSystem.

	@Test
	public void testAddRemoteSystemWithValidUserAndValidData() throws Exception {

		User validUser = new User("1","Ana","Lopez","Madrid", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario valido
		when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);

		ArrayList<Object> validData = new ArrayList<>(Arrays.asList("uno", "dos")); //dato valido
		when(mockGenericDao.updateSomeData(validUser, validData)).thenReturn(true);

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		assertDoesNotThrow(() -> manager.addRemoteSystem(validUser.getId(), validData));

		ordered.verify(mockAuthDao).getAuthData(validUser.getId());
		ordered.verify(mockGenericDao).updateSomeData(validUser, validData);
	}

	@Test
	public void testAddRemoteSystemWithValidUserAndInvalidData() throws Exception { //tambien sirve para dato nulo

		User validUser = new User("1","Ana","Lopez","Madrid", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario valido
		when(mockAuthDao.getAuthData(validUser.getId())).thenReturn(validUser);

		ArrayList<Object> invalidData = new ArrayList<>(Arrays.asList("1", "2")); //dato no valido
		when(mockGenericDao.updateSomeData(validUser, invalidData)).thenReturn(false);

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		assertThrows(SystemManagerException.class, () -> manager.addRemoteSystem(validUser.getId(), invalidData));

		ordered.verify(mockAuthDao).getAuthData(validUser.getId());
		ordered.verify(mockGenericDao).updateSomeData(validUser, invalidData);
	}

	@Test
	public void testDeleteRemoteSystemWithValidUserAndSystem() throws Exception {

		//el codigo del método sobreescribe la variable de idUser pasada por parámetro, por lo que no hace falta mockearla
		//no usa authDao, por lo que no hace falta comprobar el orden

		String validId = "12345"; // id valido de sistema
		when(mockGenericDao.deleteSomeData(any(User.class), eq(validId))).thenReturn(true);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		assertDoesNotThrow(() -> manager.deleteRemoteSystem(null, validId));

		verify(mockGenericDao).deleteSomeData(any(User.class), eq(validId));
	}

	@Test
	public void testDeleteRemoteSystemWithValidUserAndInvalidSystem() throws Exception {

		//el codigo del método sobreescribe la variable de idUser pasada por parámetro, por lo que no hace falta mockearla
		//no usa authDao, por lo que no hace falta comprobar el orden

		String invalidId = "10000"; // id NO valido de sistema
		when(mockGenericDao.deleteSomeData(any(User.class), eq(invalidId))).thenReturn(false);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		//en este caso no tiene que devolver nada, sino tirar una excepción
		assertThrows(SystemManagerException.class, () -> { manager.deleteRemoteSystem(null, invalidId);});

		verify(mockGenericDao).deleteSomeData(any(User.class), eq(invalidId));
	}


}
