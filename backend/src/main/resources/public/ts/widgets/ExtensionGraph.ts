import {idiom} from "entcore";
import http from 'axios';
import {AsyncWidget} from './AsyncWidget';

export interface GraphItem {
    _id: string;
    count: number
}

export interface ExtensionGraph {
    data: GraphItem[];
    div: string;
    series: number[];
    labels: string[];
    options: any;

    sync(): Promise<void>;

    render(): void;
}

export class ExtensionGraph extends AsyncWidget {

    constructor(div: string) {
        super();
        this.div = div;
        this.data = [];
        this.series = [];
        this.labels = [];
        this.options = {
            plotOptions: {
                pie: {
                    donut: {
                        size: '75%',
                    },
                },
                stroke: {
                    colors: undefined
                }
            },
            legend: {
                position: 'bottom'
            },
            dataLabels: {
                enabled: false
            }
        };
    }

    async sync(): Promise<void> {
        this.loading = true;
        try {
            const {data} = await http.get('/lool/monitoring/extensions');
            this.data = data;
            if (this.data.length <= 5) {
                this.data.map((object: GraphItem) => {
                    this.labels.push(object._id);
                    this.series.push(object.count);
                });
            } else {
                let otherCount = 0;
                for (let i = 0; i < 4; i++) {
                    this.labels.push(this.data[i]._id);
                    this.series.push(this.data[i].count);
                }
                for (let i = 4; i < this.data.length; i++) {
                    otherCount += this.data[i].count;
                }
                this.labels.push(idiom.translate('lool.other'));
                this.series.push(otherCount);
            }
        } catch (err) {
            throw err;
        }
        this.loading = false;
    }
}