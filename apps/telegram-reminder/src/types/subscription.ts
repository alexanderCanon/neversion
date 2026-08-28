export interface FormattedSubscription {
	id: number;
	uuid: string;
	clientName: string;
	clientEmail?: string;
	clientPhone?: string;
	serviceName: string;
	profileName?: string;
	paymentDueDate: string;
	price?: number | string | null;
	daysRemaining: number;
}

export interface RenewalWindowGroup {
	days: number;
	label: string;
	subscriptions: FormattedSubscription[];
}
