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
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestInvalidUser {

	@Mock
	private static AuthDAO mockAuthDao;
	@Mock
	private static GenericDAO mockGenericDao;

	@Test
	public void testStartRemoteSystemWithInvalidUser() throws Exception {
		//si el usuario no es valido no comprueba el sistema
		//esta prueba sirve para cualquier valor de remoteId

		User invalidUser = new User("24","Manuela","Carmena","Badajoz", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario no válido
		when(mockAuthDao.getAuthData(invalidUser.getId())).thenReturn(null);

		String validId = "12345"; // id valido de sistema
		when(mockGenericDao.getSomeData(null, "where id=" + validId)).thenThrow(new OperationNotSupportedException()); // auth es null cuando el usuario no es válido

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		 //en este caso no tiene que devolver nada, sino tirar una excepción
		assertThrows(SystemManagerException.class, () -> { manager.startRemoteSystem(invalidUser.getId(), validId);});

		ordered.verify(mockAuthDao).getAuthData(invalidUser.getId());
		ordered.verify(mockGenericDao).getSomeData(null, "where id=" + validId);
	}

	//stopRemoteSystem tiene el mismo código que startRemoteSystem,
	// por lo que los test de startRemoteSystem también prueban stopRemoteSystem.

	@Test
	public void testAddRemoteSystemWithInvalidUser() throws Exception {
		//si el usuario no es valido no comprueba el dato
		//esta prueba sirve para cualquier valor de remote

		User invalidUser = new User("24","Manuela","Carmena","Badajoz", new ArrayList<Object>(Arrays.asList(1, 2))); //Usuario no válido
		when(mockAuthDao.getAuthData(invalidUser.getId())).thenReturn(null);

		ArrayList<Object> validData = new ArrayList<>(Arrays.asList("uno", "dos")); //dato valido
		when(mockGenericDao.updateSomeData(null, validData)).thenThrow(new OperationNotSupportedException());

		InOrder ordered = inOrder(mockAuthDao, mockGenericDao);

		SystemManager manager = new SystemManager(mockAuthDao, mockGenericDao);

		assertThrows(SystemManagerException.class, () -> { manager.addRemoteSystem(invalidUser.getId(), validData);});

		ordered.verify(mockAuthDao).getAuthData(invalidUser.getId());
		ordered.verify(mockGenericDao).updateSomeData(null, validData);
	}

	//en el método deletedRemoteSystem se sobreescribe la variable de idUser pasada por parámetro
	//por lo que no hacen falta pruebas con usuario no valido

}
