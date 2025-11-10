export function mapBackendMessageToUserFriendly(message?: string): string {
  if (!message) return "An unexpected error occurred. Please try again.";

  const normalized = message.toLowerCase();

  if (normalized.includes("iata code")) {
    return "This IATA code is already in use. Please choose another one.";
  }

  if (normalized.includes("icao code")) {
    return "This ICAO code is already in use. Please choose another one.";
  }

  if (normalized.includes("no changes detected")) {
    return "No changes were detected. Please modify a field before saving.";
  }

  if (
    normalized.includes("cannot be deleted") ||
    normalized.includes("booked")
  ) {
    return "This airline cannot be deleted because it has active bookings.";
  }

  if (normalized.includes("not found")) {
    return "The requested record could not be found. It may have been deleted.";
  }

  if (normalized.includes("validation") || normalized.includes("invalid")) {
    return "Some of the entered data is invalid. Please check your inputs.";
  }

  if (normalized.includes("cannot delete flight with existing bookings")) {
    return "This flight cannot be deleted because it has existing bookings.";
  }

  if (normalized.includes("email is already registered")) {
    return "This email address is already registered. Please use another email.";
  }

  if (
    normalized.includes("bad credentials") ||
    normalized.includes("invalid credentials")
  ) {
    return "Invalid email or password. Please try again.";
  }

  return "Operation failed. Please check your input or try again.";
}
