/**
 * Obtiene la fecha formateada como YYYY-MM-DD en la zona horaria de Guatemala (America/Guatemala - UTC-6)
 * con un desplazamiento opcional en días (ej: +3 para dentro de 3 días, 0 para hoy).
 */
export function getGuatemalaDate(offsetDays: number = 0): string {
	const now = new Date();
	// Convertir a fecha relativa en zona de Guatemala
	const date = new Date(now.getTime() + offsetDays * 24 * 60 * 60 * 1000);
	return new Intl.DateTimeFormat("en-CA", {
		timeZone: "America/Guatemala",
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
	}).format(date);
}
