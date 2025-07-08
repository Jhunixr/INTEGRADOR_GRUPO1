function confirmarReserva() {
  const fecha = document.getElementById('datePicker').value;
  const paquete = document.getElementById('packageSelect').value;

  if (!fecha) {
    alert('Por favor, selecciona una fecha para tu evento.');
    return;
  }

  alert(`✅ Reserva confirmada para el ${fecha} con el paquete: ${paquete}`);
}