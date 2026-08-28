// Helper to unwrap objects/arrays from Supabase join results
export const firstObj = <T>(val: T | T[] | null | undefined): T | null => {
	if (!val) return null;
	return Array.isArray(val) ? val[0] : val;
};
