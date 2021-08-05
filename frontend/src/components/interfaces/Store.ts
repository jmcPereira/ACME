export default interface Store {
    id: number;
    code: string;
    description: string;
    name: string;
    openingDate: string;
    storeType: string;
    seasons: Season[];
    additionalInfo: {specialField1: string, specialField2: string};
}

interface Season{
    id: number;
    season: string;
}